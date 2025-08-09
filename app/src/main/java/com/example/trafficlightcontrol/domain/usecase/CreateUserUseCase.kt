package com.example.trafficlightcontrol.domain.usecase

import com.example.trafficlightcontrol.domain.model.ActionLog
import com.example.trafficlightcontrol.domain.model.ActionType
import com.example.trafficlightcontrol.domain.model.User
import com.example.trafficlightcontrol.domain.model.UserRole
import com.example.trafficlightcontrol.domain.repo.LogRepository
import com.example.trafficlightcontrol.domain.repo.UserRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject

class CreateUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val logRepository: LogRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        name: String,
        role: UserRole
    ): Result<User> {
        val currentUser = userRepository.getCurrentUser().first()
            ?: return Result.failure(Exception("Admin not authenticated"))

        if (currentUser.role != UserRole.ADMIN) {
            return Result.failure(Exception("Only admin can create users"))
        }

        val result = userRepository.createUser(email, password, name, role)

        result.onSuccess { newUser ->
            logRepository.addLog(
                ActionLog(
                    userId = currentUser.id,
                    userName = currentUser.name,
                    userRole = currentUser.role,
                    timestamp = LocalDateTime.now(),
                    actionType = ActionType.USER_MANAGEMENT,
                    description = "Created new user: $name (${role.name})",
                    newState = "User ID: ${newUser.id}, Email: $email, Role: ${role.name}"
                )
            )
        }

        return result
    }
}