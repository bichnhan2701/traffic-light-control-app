package com.example.trafficlightcontrol.data.model

import com.example.trafficlightcontrol.domain.model.ActionType
import com.example.trafficlightcontrol.domain.model.UserRole

data class ActionLogDto(
    val userId: String = "",
    val userName: String = "",
    val userRole: UserRole = UserRole.OPERATOR,
    val timestamp: String = "",
    val actionType: ActionType = ActionType.PHASE_CHANGE,
    val description: String = "",
    val previousState: String = "",
    val newState: String = ""
)