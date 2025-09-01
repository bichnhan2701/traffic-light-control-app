package com.example.trafficlightcontrol.data.model

/* =========================
 *  Domain models
 * ========================= */

enum class Mode {
    default, night, emergency_A, emergency_B, peak;

    companion object {
        fun from(s: String?): Mode = when (s) {
            "night" -> night
            "emergency_A" -> emergency_A
            "emergency_B" -> emergency_B
            "peak" -> peak
            else -> default
        }
    }
}

enum class Phase {
    A_GREEN, A_YELLOW, ALL_RED_A2B, B_GREEN, B_YELLOW, ALL_RED_B2A, UNKNOWN;

    companion object {
        fun from(s: String?): Phase = try { valueOf(s ?: "") } catch (_: Exception) { A_GREEN }
    }
}

/** Thời lượng gửi từ ESP (ms). */
data class Durations(
    val greenA_ms: Int = 5000,
    val greenB_ms: Int = 5000,
    val yellow_ms: Int = 3000,
    val clear_ms:  Int = 1000
)

/** /reported (ESP ghi khi có sự kiện). KHÔNG có lights/timeLeft_ms. */
data class Reported(
    val mode: Mode = Mode.default,
    val emergencyPriority: String? = null, // "A" | "B" | null
    val phase: Phase = Phase.A_GREEN,
    val phaseStartAt: Long = 0L,           // server timestamp (ms)
    val durations: Durations = Durations(),
    val peakGreenA_s: Int? = null,
    val peakGreenB_s: Int? = null,
    val ack: Ack? = null
)

/** /reported/ack (ESP phản hồi desired gần nhất) */
data class Ack(
    val requestId: String = "",
    val desiredVersion: Long = -1,
    val status: String = "",              // "applied" | "rejected"
    val reason: String? = null
)

/** /connection/esp trạng thái kết nối thiết bị */
data class ConnectionState(
    val online: Boolean = false,
    val lastSeenAt: Long = 0L,
    val ip: String? = null,
    val rssi: Int? = null,
    val espId: String? = null,
    val fw: String? = null
)

/** Màu đèn UI suy diễn tại client */
data class UiLights(
    val A_red: Boolean = false,
    val A_yellow: Boolean = false,
    val A_green: Boolean = false,
    val B_red: Boolean = false,
    val B_yellow: Boolean = false,
    val B_green: Boolean = false
)

/** Trạng thái hiển thị cuối cùng cho UI (mô phỏng cục bộ) */
data class UiState(
    val mode: Mode,
    val phase: Phase,
    val serverNow: Long,      // now đã cộng offset
    val timeLeftMs: Long,     // client tính từ phaseStartAt + durations
    val totalPhaseMs: Long,   // tổng ms của pha hiện tại
    val lights: UiLights,
    val ack: Ack? = null,
    val durations: Durations
)

enum class LogType { MODE_START, MODE_END, UNKNOWN }

data class LogEntry(
    val id: String,
    val type: LogType,
    val mode: Mode?,     // default | night | emergency_A | emergency_B | peak
    val ts: Long,        // startedAt || endedAt
    val source: String?,
    val greenA_s: Int?,  // cho peak
    val greenB_s: Int?
)