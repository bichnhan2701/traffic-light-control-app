package com.example.trafficlightcontrol.domain.model

import java.time.LocalDateTime

enum class ActionType {
    MANUAL_OVERRIDE,
    PHASE_CHANGE,
    CONFIG_UPDATE,
    USER_MANAGEMENT,
    LOGIN,
    LOGOUT
}

data class ActionLog(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userRole: UserRole = UserRole.OPERATOR,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val actionType: ActionType,
    val description: String = "",
    val previousState: String = "", // JSON serialized state before action
    val newState: String = ""       // JSON serialized state after action
)