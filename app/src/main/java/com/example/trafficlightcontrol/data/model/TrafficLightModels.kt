package com.example.trafficlightcontrol.data.model

import com.google.firebase.database.IgnoreExtraProperties

enum class TrafficLightMode {
    SCHEDULED, PEAK, NIGHT, EMERGENCY
}

enum class TrafficLightPhase {
    A, B
}

@IgnoreExtraProperties
data class TrafficLightCurrent(
    val mode: String = "",
    val phase: String = "",
    val cycle_time: Int = 0,
    val time_left: Int = 0,
    val last_updated_by: String = "",
    val timestamp: Long = 0
)

@IgnoreExtraProperties
data class ScheduleEntry(
    val id: String = "",
    val start_time: String = "",
    val end_time: String = "",
    val mode: String = "",
    val cycle_time: Int = 0,
    val green_time_a: Int = 0,
    val green_time_b: Int = 0
)

@IgnoreExtraProperties
data class EmergencyStatus(
    val active: Boolean = false,
    val priority_direction: String = "",
    val activated_by: String = "",
    val timestamp: Long = 0
)

@IgnoreExtraProperties
data class LogEntry(
    val id: String = "",
    val action: String = "",
    val from: String = "",
    val to: String = "",
    val by: String = "",
    val timestamp: Long = 0
)

@IgnoreExtraProperties
data class SystemStatus(
    val sync_time: Long = 0,
    val esp32_connected: Boolean = false
)