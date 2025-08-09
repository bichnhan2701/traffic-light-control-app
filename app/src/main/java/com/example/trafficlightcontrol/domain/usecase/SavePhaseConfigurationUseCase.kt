package com.example.trafficlightcontrol.domain.usecase

import com.example.trafficlightcontrol.domain.model.ActionLog
import com.example.trafficlightcontrol.domain.model.ActionType
import com.example.trafficlightcontrol.domain.model.PhaseConfiguration
import com.example.trafficlightcontrol.domain.repo.LogRepository
import com.example.trafficlightcontrol.domain.repo.TrafficLightRepository
import com.example.trafficlightcontrol.domain.repo.UserRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject

class SavePhaseConfigurationUseCase @Inject constructor(
    private val trafficLightRepository: TrafficLightRepository,
    private val userRepository: UserRepository,
    private val logRepository: LogRepository
) {
    suspend operator fun invoke(config: PhaseConfiguration): Result<Unit> {
        val currentUser = userRepository.getCurrentUser().first()
            ?: return Result.failure(Exception("User not authenticated"))

        val result = trafficLightRepository.savePhaseConfiguration(config)

        result.onSuccess {
            // Log the action
            logRepository.addLog(
                ActionLog(
                    userId = currentUser.id,
                    userName = currentUser.name,
                    userRole = currentUser.role,
                    timestamp = LocalDateTime.now(),
                    actionType = ActionType.CONFIG_UPDATE,
                    description = "Updated phase configuration: ${config.name}",
                    newState = "Red: ${config.redDuration}s, Yellow: ${config.yellowDuration}s, Green: ${config.greenDuration}s"
                )
            )
        }

        return result
    }
}