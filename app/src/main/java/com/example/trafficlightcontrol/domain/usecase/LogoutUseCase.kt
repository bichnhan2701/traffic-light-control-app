package com.example.trafficlightcontrol.domain.usecase

import com.example.trafficlightcontrol.domain.model.ActionLog
import com.example.trafficlightcontrol.domain.model.ActionType
import com.example.trafficlightcontrol.domain.repo.LogRepository
import com.example.trafficlightcontrol.domain.repo.UserRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val logRepository: LogRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        // Get current user before logout
        val currentUser = userRepository.getCurrentUser().first()

        val result = userRepository.logout()

        if (currentUser != null) {
            // Log the logout action
            logRepository.addLog(
                ActionLog(
                    userId = currentUser.id,
                    userName = currentUser.name,
                    userRole = currentUser.role,
                    timestamp = LocalDateTime.now(),
                    actionType = ActionType.LOGOUT,
                    description = "User logged out"
                )
            )
        }

        return result
    }
}