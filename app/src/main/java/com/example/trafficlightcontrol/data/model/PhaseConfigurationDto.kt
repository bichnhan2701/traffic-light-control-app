package com.example.trafficlightcontrol.data.model

data class PhaseConfigurationDto(
    val startTime: String = "00:00",
    val endTime: String = "23:59",
    val redDuration: Int = 60,
    val yellowDuration: Int = 5,
    val greenDuration: Int = 55,
    val name: String = "Default"
)