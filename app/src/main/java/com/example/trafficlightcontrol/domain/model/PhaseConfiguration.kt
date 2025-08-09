package com.example.trafficlightcontrol.domain.model

import java.time.LocalTime

data class TimeRange(
    val start: LocalTime,
    val end: LocalTime
)

data class PhaseConfiguration(
    val id: String = "",
    val timeRange: TimeRange,
    val redDuration: Int = 60,  // seconds
    val yellowDuration: Int = 5,  // seconds
    val greenDuration: Int = 55,  // seconds
    val name: String = "Default"  // e.g., "Morning Rush", "Evening", etc.
)