package com.example.trafficlightcontrol.domain.model

enum class UserRole {
    ADMIN,
    OPERATOR
}

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: UserRole = UserRole.OPERATOR,
    val isActive: Boolean = true
)