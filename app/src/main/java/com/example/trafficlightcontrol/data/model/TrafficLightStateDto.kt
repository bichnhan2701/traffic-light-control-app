package com.example.trafficlightcontrol.data.model

import com.example.trafficlightcontrol.domain.model.LightPhase

data class TrafficLightStateDto(
    val currentPhase: LightPhase = LightPhase.RED,
    val remainingTime: Int = 0,
    val isManualOverride: Boolean = false
)