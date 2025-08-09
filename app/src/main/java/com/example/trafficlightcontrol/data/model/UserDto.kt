package com.example.trafficlightcontrol.data.model

import com.example.trafficlightcontrol.domain.model.UserRole

data class UserDto(
    val email: String = "",
    val name: String = "",
    val role: UserRole = UserRole.OPERATOR,
    val isActive: Boolean = true
)