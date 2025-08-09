package com.example.trafficlightcontrol.domain.usecase

import com.example.trafficlightcontrol.domain.model.ActionLog
import com.example.trafficlightcontrol.domain.model.ActionType
import com.example.trafficlightcontrol.domain.model.User
import com.example.trafficlightcontrol.domain.repo.LogRepository
import com.example.trafficlightcontrol.domain.repo.UserRepository
import java.time.LocalDateTime
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val logRepository: LogRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        val result = userRepository.login(email, password)

        result.onSuccess { user ->
            // Log the login action
            logRepository.addLog(
                ActionLog(
                    userId = user.id,
                    userName = user.name,
                    userRole = user.role,
                    timestamp = LocalDateTime.now(),
                    actionType = ActionType.LOGIN,
                    description = "User logged in"
                )
            )
        }

        return result
    }
}