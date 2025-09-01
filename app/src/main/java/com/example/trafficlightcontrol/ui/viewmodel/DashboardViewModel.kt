package com.example.trafficlightcontrol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trafficlightcontrol.data.model.*
import com.example.trafficlightcontrol.data.repository.TrafficLightRepository
import com.example.trafficlightcontrol.data.repository.valueEvents
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

/* ---------- UI models for dashboard ---------- */

data class AppConnectionState(
    val online: Boolean = false,
    val lastSeenAt: Long = 0L,
    val deviceId: String? = null,
    val platform: String? = null,
    val appVer: String? = null
)

data class PhasePanel(
    val mode: Mode,
    val phase: Phase,
    val timeLeftMs: Long,
    val totalMs: Long,
    val progress: Float,      // 0f..1f
    val timeLeftText: String, // mm:ss
    val totalText: String     // mm:ss
)

data class LightColumns(
    val A_red: Boolean,
    val A_yellow: Boolean,
    val A_green: Boolean,
    val B_red: Boolean,
    val B_yellow: Boolean,
    val B_green: Boolean
)

data class DashboardUiState(
    val esp: ConnectionState? = null,
    val app: AppConnectionState? = null,
    val lights: LightColumns? = null,
    val phasePanel: PhasePanel? = null,
    val ack: Ack? = null,
    val durations: Durations? = null,
    val serverNow: Long = 0L,            // ✅ thêm: dùng cho header tính “ago”
    val isLoading: Boolean = true,
    val error: String? = null,
)

/* ---------- ViewModel ---------- */

class DashboardViewModel(
    private val db: FirebaseDatabase,
    private val repo: TrafficLightRepository,
    private val intersectionId: String
) : ViewModel() {

    private var guardianJob: Job? = null

    init {
        // ✅ bật guardian để app tự hạ cờ /connection/esp/online=false khi stale
        guardianJob?.cancel()
        guardianJob = repo.startAutoOfflineGuardian(intersectionId)
    }

    /** /connection/app mapper (nhẹ) */
    private fun appConnFlow(): Flow<AppConnectionState> =
        db.getReference("/traffic/intersections/$intersectionId/connection/app")
            .valueEvents()
            .map { s: DataSnapshot ->
                AppConnectionState(
                    online     = s.child("online").getValue(Boolean::class.java) == true,
                    lastSeenAt = s.child("lastSeenAt").getValue(Long::class.java) ?: 0L,
                    deviceId   = s.child("info/deviceId").getValue(String::class.java),
                    platform   = s.child("info/platform").getValue(String::class.java),
                    appVer     = s.child("info/appVer").getValue(String::class.java)
                )
            }
            .distinctUntilChanged()

    /** Kết nối ESP (đã là “online hiệu lực”) */
    private val espConnFlow: Flow<ConnectionState> =
        repo.liveConnectionFlow(intersectionId)

    /** Đồng hồ server (chuẩn theo repo) */
    private val serverNowFlow: Flow<Long> =
        repo.serverNowFlow

    /** Trạng thái UI mô phỏng realtime (đã gồm serverOffset + ticker) */
    private val runtimeUiFlow: Flow<UiState> =
        repo.uiStateFlow(intersectionId, uiTickMs = 150L)

    /** State public cho màn hình */
    val uiState: StateFlow<DashboardUiState> =
        combine(
            runtimeUiFlow,
            espConnFlow.onStart { emit(ConnectionState()) }.catch { emit(ConnectionState()) },
            appConnFlow().onStart { emit(AppConnectionState()) }.catch { emit(AppConnectionState()) },
            serverNowFlow.onStart { emit(0L) }.catch { emit(0L) }
        ) { ui: UiState, esp: ConnectionState, app: AppConnectionState, serverNow: Long ->
            val lights = LightColumns(
                A_red = ui.lights.A_red,
                A_yellow = ui.lights.A_yellow,
                A_green = ui.lights.A_green,
                B_red = ui.lights.B_red,
                B_yellow = ui.lights.B_yellow,
                B_green = ui.lights.B_green
            )

            val total = max(0L, ui.totalPhaseMs)
            val left  = min(max(0L, ui.timeLeftMs), total)
            val prog  = if (total > 0) 1f - (left.toFloat() / total.toFloat()) else 0f

            DashboardUiState(
                esp = esp,
                app = app,
                lights = lights,
                phasePanel = PhasePanel(
                    mode = ui.mode,
                    phase = ui.phase,
                    timeLeftMs = left,
                    totalMs = total,
                    progress = prog,
                    timeLeftText = left.asMmSs(),
                    totalText = total.asMmSs()
                ),
                ack = ui.ack,
                durations = ui.durations,
                serverNow = serverNow,            // ✅ đưa vào UI state
                isLoading = false,
                error = null
            )
        }
            .onStart { emit(DashboardUiState(isLoading = true)) }
            .catch { e -> emit(DashboardUiState(isLoading = false, error = e.message)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState(isLoading = true))

    /* ---------- Intent: gửi lệnh điều khiển ---------- */

    fun setDefault() = viewModelScope.launch {
        repo.setDefault(intersectionId)
    }

    fun setNight() = viewModelScope.launch {
        repo.setNight(intersectionId)
    }

    fun setEmergencyA() = viewModelScope.launch {
        repo.setEmergencyA(intersectionId)   // ✅ dùng API mới
    }

    fun setEmergencyB() = viewModelScope.launch {
        repo.setEmergencyB(intersectionId)   // ✅ dùng API mới
    }

    fun setPeak(greenA_s: Int, greenB_s: Int) = viewModelScope.launch {
        repo.setPeak(intersectionId, greenA_s, greenB_s)
    }
}

/* ---------- Helpers ---------- */

private fun Long.asMmSs(): String {
    val totalSec = (this / 1000).toInt()
    val m = totalSec / 60
    val s = totalSec % 60
    return "%02d:%02d".format(m, s)
}