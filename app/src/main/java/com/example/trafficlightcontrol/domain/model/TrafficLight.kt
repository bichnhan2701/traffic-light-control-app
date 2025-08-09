package com.example.trafficlightcontrol.domain.model

enum class LightPhase {
    RED,
    YELLOW,
    GREEN
}

enum class Direction {
    NORTH_SOUTH,
    EAST_WEST
}

data class TrafficLightState(
    val id: String = "",
    val direction: Direction = Direction.NORTH_SOUTH,
    var currentPhase: LightPhase = LightPhase.RED,
    var remainingTime: Int = 0,  // in seconds
    var isManualOverride: Boolean = false
)