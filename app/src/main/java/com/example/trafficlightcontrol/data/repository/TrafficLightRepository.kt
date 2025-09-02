package com.example.trafficlightcontrol.data.repository

import com.example.trafficlightcontrol.data.helper.Paths
import com.example.trafficlightcontrol.data.helper.deriveLights
import com.example.trafficlightcontrol.data.helper.phaseTotalMs
import com.example.trafficlightcontrol.data.model.*
import com.example.trafficlightcontrol.ui.viewmodel.LightColumns
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Query
import com.google.firebase.database.ServerValue
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max

/* =========================
 *  Mapping helpers
 * ========================= */

private fun DataSnapshot.optString(path: String): String? =
    this.child(path).getValue(String::class.java)

private fun DataSnapshot.optInt(path: String): Int? =
    this.child(path).getValue(Int::class.java)

private fun DataSnapshot.optLong(path: String): Long? =
    this.child(path).getValue(Long::class.java)

private fun snapshotToReported(s: DataSnapshot): Reported {
    val mode = Mode.from(s.optString("mode"))
    val phase = Phase.from(s.optString("phase"))
    val phaseStartAt = s.optLong("phaseStartAt") ?: 0L

    val durations = Durations(
        greenA_ms = s.optInt("durations/greenA_ms") ?: 5000,
        greenB_ms = s.optInt("durations/greenB_ms") ?: 5000,
        yellow_ms = s.optInt("durations/yellow_ms") ?: 3000,
        clear_ms  = s.optInt("durations/clear_ms")  ?: 1000
    )

    val ep = s.optString("emergencyPriority")

    val a = s.child("ack")
    val ack = if (a.exists()) Ack(
        requestId = a.optString("requestId") ?: "",
        desiredVersion = a.optLong("desiredVersion") ?: -1L,
        status = a.optString("status") ?: "",
        reason = a.optString("reason")
    ) else null

    val peakA = s.optInt("peak/greenA_s")
    val peakB = s.optInt("peak/greenB_s")

    return Reported(
        mode = mode,
        emergencyPriority = ep,
        phase = phase,
        phaseStartAt = phaseStartAt,
        durations = durations,
        peakGreenA_s = peakA,
        peakGreenB_s = peakB,
        ack = ack
    )
}

private fun snapshotToConnection(s: DataSnapshot): ConnectionState =
    ConnectionState(
        online     = s.child("online").getValue(Boolean::class.java) == true,
        lastSeenAt = s.child("lastSeenAt").getValue(Long::class.java) ?: 0L,
        ip         = s.child("info/ip").getValue(String::class.java),
        rssi       = s.child("info/rssi").getValue(Int::class.java),
        espId      = s.child("info/espId").getValue(String::class.java),
        // chấp nhận cả info/fw (cũ) và fwVersion (mới, nếu được ghi bởi ESP)
        fw         = s.child("info/fw").getValue(String::class.java)
            ?: s.child("fwVersion").getValue(String::class.java)
    )

fun LightColumns.toUiLights(): UiLights =
    UiLights(
        A_red = A_red, A_yellow = A_yellow, A_green = A_green,
        B_red = B_red, B_yellow = B_yellow, B_green = B_green
    )

fun String?.toLogType(): LogType = when (this?.lowercase()) {
    "mode_start" -> LogType.MODE_START
    "mode_end"   -> LogType.MODE_END
    else         -> LogType.UNKNOWN
}

//fun String?.toModeOrNull(): Mode? = try {
//    this?.uppercase()?.let { Mode.valueOf(it) }
//} catch (_: Throwable) { null }

/* =========================
 *  Firebase tiện ích Flow
 * ========================= */

fun DatabaseReference.valueEvents(): Flow<DataSnapshot> = callbackFlow {
    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) { trySend(snapshot).isSuccess }
        override fun onCancelled(error: DatabaseError) { close(error.toException()) }
    }
    addValueEventListener(listener)
    awaitClose { removeEventListener(listener) }
}

fun Query.valueEvents(): Flow<DataSnapshot> = callbackFlow {
    val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) { trySend(snapshot).isSuccess }
        override fun onCancelled(error: DatabaseError) { close(error.toException()) }
    }
    addValueEventListener(listener)
    awaitClose { removeEventListener(listener) }
}

/** Ticker local để render UI (không gọi Firebase). */
fun tickerFlow(periodMs: Long): Flow<Long> = flow {
    while (true) {
        emit(System.currentTimeMillis())
        delay(periodMs)
    }
}.distinctUntilChanged()

/* =========================
 *  Repository
 * ========================= */

private const val BEAT_MS  = 20_000L                  // ESP gửi heartbeat mỗi 20s
private const val STALE_MS = BEAT_MS                  // hạ cờ sau 20s nếu không có heartbeat

class TrafficLightRepository(
    internal val db: FirebaseDatabase,
    private val appScope: CoroutineScope
) {

    /** Server time offset → dùng tính serverNow = now + offset */
    val serverOffsetFlow: Flow<Long> =
        Paths.infoOffset(db).valueEvents()
            .map { snap -> snap.getValue(Long::class.java) ?: 0L }
            .distinctUntilChanged()

    /** Đồng hồ serverNow = System.currentTimeMillis + offset (tick 1s) */
    val serverNowFlow: Flow<Long> =
        combine(serverOffsetFlow, tickerFlow(1_000L)) { offset, _ ->
            System.currentTimeMillis() + offset
        }.distinctUntilChanged()

    /** Lắng nghe /reported (ít sự kiện) */
    fun reportedFlow(intersectionId: String): Flow<Reported> =
        Paths.reported(db, intersectionId).valueEvents()
            .map { snap -> snapshotToReported(snap) }
            .distinctUntilChanged()

    /** Lắng nghe /connection/esp (raw: online, lastSeenAt…) */
    fun connectionFlow(intersectionId: String): Flow<ConnectionState> =
        Paths.connEsp(db, intersectionId).valueEvents()
            .map { snap -> snapshotToConnection(snap) }
            .distinctUntilChanged()

    /** Online hiệu lực cho UI: true nếu raw.online && (now - lastSeenAt) < STALE_MS */
    fun liveConnectionFlow(intersectionId: String): Flow<ConnectionState> =
        combine(connectionFlow(intersectionId), serverNowFlow) { conn, now ->
            conn.copy(online = conn.online && (now - conn.lastSeenAt) < STALE_MS)
        }.distinctUntilChanged()

    /**
     * Flow trạng thái UI realtime:
     * - kết hợp /reported (event), serverOffset và ticker local (uiTickMs)
     * - KHÔNG đọc Firebase mỗi tick
     */
    fun uiStateFlow(
        intersectionId: String,
        uiTickMs: Long = 150L
    ): Flow<UiState> {
        val rep: Flow<Reported> = reportedFlow(intersectionId)
        val off: Flow<Long> = serverOffsetFlow
        val tick: Flow<Long> = tickerFlow(uiTickMs)

        return combine(rep, off, tick) { reported, offset, _ ->
            val serverNow: Long = System.currentTimeMillis() + offset
            val total: Long = phaseTotalMs(reported.phase, reported.mode, reported.durations)
            val elapsed: Long = max(0L, serverNow - reported.phaseStartAt)
            val timeLeft: Long = max(0L, total - elapsed)
            UiState(
                mode = reported.mode,
                phase = reported.phase,
                serverNow = serverNow,
                timeLeftMs = timeLeft,
                totalPhaseMs = total,
                lights = deriveLights(reported.mode, reported.phase, serverNow),
                ack = reported.ack,
                durations = reported.durations
            )
        }.distinctUntilChanged()
    }

    /* ------------- Gửi lệnh /desired ------------- */

    /** Tạo version kế tiếp (đọc version hiện tại rồi +1). */
    private suspend fun nextVersion(desiredRef: DatabaseReference): Long =
        withContext(Dispatchers.IO) {
            val snap: DataSnapshot = desiredRef.child("meta/version").get().await()
            val cur: Long = snap.getValue(Long::class.java) ?: -1L
            cur + 1L
        }

    private suspend fun writeDesired(
        desiredRef: DatabaseReference,
        payload: Map<String, Any?>
    ): Pair<String, Long> = withContext(Dispatchers.IO) {
        val requestId = UUID.randomUUID().toString()
        val version = nextVersion(desiredRef)

        val meta = mapOf(
            "requestedBy" to "android",             // có thể thay bằng deviceId/userId
            "requestedAt" to ServerValue.TIMESTAMP,
            "requestId"   to requestId,
            "version"     to version
        )

        val body = payload.toMutableMap().apply { put("meta", meta) }

        suspendCancellableCoroutine<Pair<String, Long>> { cont ->
            desiredRef.updateChildren(body)
                .addOnSuccessListener { cont.resume(requestId to version) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
        }
    }

    /** Chờ ack matching requestId+version, hoặc timeout ms (poll nhẹ). */
    internal suspend fun awaitAck(
        reportedRef: DatabaseReference,
        requestId: String,
        version: Long,
        timeoutMs: Long = 3000
    ): Ack? = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        var last: Ack? = null
        while (System.currentTimeMillis() - start < timeoutMs) {
            val snap: DataSnapshot = reportedRef.child("ack").get().await()
            val rid: String = snap.child("requestId").getValue(String::class.java) ?: ""
            val ver: Long = snap.child("desiredVersion").getValue(Long::class.java) ?: -1L
            val st: String = snap.child("status").getValue(String::class.java) ?: ""
            val rs: String? = snap.child("reason").getValue(String::class.java)

            if (rid == requestId && ver == version && st.isNotEmpty()) {
                return@withContext Ack(rid, ver, st, rs)
            }
            last = Ack(rid, ver, st, rs)
            delay(100)
        }
        last
    }

    private suspend fun readReportedOnce(
        reportedRef: DatabaseReference
    ): Reported = withContext(Dispatchers.IO) {
        val snap = reportedRef.get().await()
        snapshotToReported(snap)
    }

    private fun pushLog(logsRef: DatabaseReference, payload: Map<String, Any?>) {
        logsRef.push().updateChildren(payload)
    }

    private fun logModeEnd(
        db: FirebaseDatabase,
        intersectionId: String,
        prevMode: Mode,
        prevDurations: Durations?, // nếu có
        extra: Map<String, Any?> = emptyMap()
    ) {
        val logsRef = Paths.logs(db, intersectionId)
        val base = mutableMapOf<String, Any?>(
            "type" to "mode_end",
            "mode" to prevMode.name,               // "default" | "night" | "emergency_A" | "emergency_B" | "peak"
            "endedAt" to ServerValue.TIMESTAMP,
            "source" to "android"
        )
        prevDurations?.let {
            base["durations_ms"] = mapOf(
                "greenA_ms" to it.greenA_ms,
                "greenB_ms" to it.greenB_ms,
                "yellow_ms" to it.yellow_ms,
                "clear_ms"  to it.clear_ms
            )
        }
        base.putAll(extra)
        pushLog(logsRef, base)
    }

    private fun logModeStart(
        db: FirebaseDatabase,
        intersectionId: String,
        newMode: Mode,
        extra: Map<String, Any?> = emptyMap()
    ) {
        val logsRef = Paths.logs(db, intersectionId)
        val base = mutableMapOf<String, Any?>(
            "type" to "mode_start",
            "mode" to newMode.name,                // đúng chuỗi enum
            "startedAt" to ServerValue.TIMESTAMP,
            "source" to "android"
        )
        base.putAll(extra)
        pushLog(logsRef, base)
    }

    /* ---- APIs gửi lệnh ---- */
    // Dùng chung
    private suspend fun setModeOnly(
        intersectionId: String,
        newMode: Mode,
        extraPayload: Map<String, Any?> = emptyMap(),
        onApplied: (suspend () -> Unit)? = null
    ): Ack? {
        val desired  = Paths.desired(db, intersectionId)
        val reported = Paths.reported(db, intersectionId)

        val prev = readReportedOnce(reported)
        if (prev.mode != newMode) {
            logModeEnd(db, intersectionId, prev.mode, prev.durations)
        }

        val payload = buildMap<String, Any?> {
            put("mode", newMode.name)     // CHỐT: lưu y hệt enum
            put("peak", null)             // sẽ override ở setPeak
            putAll(extraPayload)
        }

        val (rid, ver) = writeDesired(desired, payload)
        val ack = awaitAck(reported, rid, ver)
        if (ack?.status == "applied") {
            logModeStart(db, intersectionId, newMode)
            onApplied?.invoke()
        }
        return ack
    }

    suspend fun setDefault(intersectionId: String): Ack? =
        setModeOnly(intersectionId, Mode.default)

    suspend fun setNight(intersectionId: String): Ack? =
        setModeOnly(intersectionId, Mode.night)

    // ✅ Emergency tách A/B
    suspend fun setEmergencyA(intersectionId: String): Ack? =
        setModeOnly(intersectionId, Mode.emergency_A)

    suspend fun setEmergencyB(intersectionId: String): Ack? =
        setModeOnly(intersectionId, Mode.emergency_B)

    // (Tuỳ chọn) Giữ API cũ để tương thích
    @Deprecated("Use setEmergencyA/B instead")
    suspend fun setEmergency(intersectionId: String, priority: String /* A | B */): Ack? =
        when (priority.uppercase(Locale.ROOT)) {
            "A" -> setEmergencyA(intersectionId)
            else -> setEmergencyB(intersectionId)
        }

    // Peak với tham số
    suspend fun setPeak(intersectionId: String, greenA_s: Int, greenB_s: Int): Ack? =
        setModeOnly(
            intersectionId = intersectionId,
            newMode = Mode.peak,
            extraPayload = mapOf(
                "peak" to mapOf(
                    "greenA_s" to greenA_s,
                    "greenB_s" to greenB_s
                )
            ),
            onApplied = {
                // peak: log kèm thông số để hiển thị
                val logsRef = Paths.logs(db, intersectionId)
                logsRef.push().updateChildren(
                    mapOf(
                        "type" to "mode_start",
                        "mode" to Mode.peak.name,
                        "startedAt" to ServerValue.TIMESTAMP,
                        "source" to "android",
                        "greenA_s" to greenA_s,
                        "greenB_s" to greenB_s
                    )
                )
            }
        )

    /* ----------------- Logs ----------------- */

    fun logsFlow(
        intersectionId: String,
        limit: Int = 200
    ): Flow<List<LogEntry>> =
        Paths.logs(db, intersectionId)     // DatabaseReference
            .limitToLast(limit)            // → Query
            .valueEvents()                 // dùng overload cho Query
            .map { snap ->
                snap.children.map { c -> snapshotToLog(c) }
                    .sortedByDescending { it.ts } // mới nhất lên đầu
            }

    private fun snapshotToLog(snap: DataSnapshot): LogEntry {
        val id = snap.key ?: ""
        val type = snap.child("type").getValue(String::class.java).toLogType()

        val modeStr = snap.child("mode").getValue(String::class.java)
        val mode = Mode.from(modeStr)

        val startedAt = snap.child("startedAt").getValue(Long::class.java)
        val endedAt   = snap.child("endedAt").getValue(Long::class.java)
        val ts = startedAt ?: endedAt ?: 0L

        val source   = snap.child("source").getValue(String::class.java)
        val greenA_s = snap.child("greenA_s").getValue(Long::class.java)?.toInt()
        val greenB_s = snap.child("greenB_s").getValue(Long::class.java)?.toInt()

        return LogEntry(
            id = id,
            type = type,
            mode = mode,
            ts = ts,
            source = source,
            greenA_s = greenA_s,
            greenB_s = greenB_s
        )
    }

    /* ----------------- Guardian: tự hạ cờ online=false ----------------- */

    /** Phát ra serverNow khi raw.online && stale ≥ 20s; nếu không thì im lặng */
    private fun staleTickFlow(intersectionId: String): Flow<Long> =
        combine(connectionFlow(intersectionId), serverNowFlow) { conn, now ->
            if (conn.online && (now - conn.lastSeenAt) >= STALE_MS) now else null
        }
            .filterNotNull()
            .distinctUntilChanged()

    /** Transaction hạ cờ; an toàn nếu ESP vừa alive (transaction sẽ abort) */
    private suspend fun markOfflineIfStaleTx(
        intersectionId: String,
        serverNowMs: Long
    ) {
        val ref = Paths.connEsp(db, intersectionId) // .../connection/esp
        suspendCancellableCoroutine<Unit> { cont ->
            ref.runTransaction(object : Transaction.Handler {
                override fun doTransaction(current: MutableData): Transaction.Result {
                    val online = current.child("online").getValue(Boolean::class.java) ?: false
                    val last   = current.child("lastSeenAt").getValue(Long::class.java) ?: 0L
                    val stale  = (serverNowMs - last) >= STALE_MS

                    return if (online && stale) {
                        current.child("online").value     = false
                        current.child("staleSince").value = serverNowMs // optional
                        current.child("resetBy").value    = "app"       // optional
                        Transaction.success(current)
                    } else {
                        Transaction.abort()
                    }
                }

                override fun onComplete(
                    error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?
                ) {
                    if (error != null) cont.resumeWithException(error.toException())
                    else cont.resume(Unit)
                }
            })
        }
    }

    /** Bắt đầu giám sát và tự hạ online=false bằng transaction khi stale (chạy nền) */
    fun startAutoOfflineGuardian(intersectionId: String): Job =
        staleTickFlow(intersectionId)
            .onEach { now ->                      // Flow.onEach (đúng import kotlinx.coroutines.flow.onEach)
                markOfflineIfStaleTx(intersectionId, now)
            }
            .launchIn(appScope)                   // chạy trong coroutine scope (fix lỗi "suspend ... only from coroutine")
}

/* =========================
 *  Task<T>.await() helper
 * ========================= */
private suspend fun <T> Task<T>.await(): T =
    suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result -> cont.resume(result) }
        addOnFailureListener { e -> cont.resumeWithException(e) }
    }