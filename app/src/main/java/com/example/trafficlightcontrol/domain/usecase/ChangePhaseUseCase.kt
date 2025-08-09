package com.example.trafficlightcontrol.domain.usecase

import com.example.trafficlightcontrol.domain.model.ActionLog
import com.example.trafficlightcontrol.domain.model.ActionType
import com.example.trafficlightcontrol.domain.model.Direction
import com.example.trafficlightcontrol.domain.model.LightPhase
import com.example.trafficlightcontrol.domain.repo.LogRepository
import com.example.trafficlightcontrol.domain.repo.TrafficLightRepository
import com.example.trafficlightcontrol.domain.repo.UserRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject

class ChangePhaseUseCase @Inject constructor(
    private val trafficLightRepository: TrafficLightRepository,
    private val userRepository: UserRepository,
    private val logRepository: LogRepository
) {
    suspend operator fun invoke(direction: Direction, phase: LightPhase): Result<Unit> {
        val currentUser = userRepository.getCurrentUser().first()
            ?: return Result.failure(Exception("User not authenticated"))

        // Get current state before changing
        val currentState = trafficLightRepository.observeTrafficLightState(direction).first()

        val result = trafficLightRepository.changePhase(direction, phase)

        result.onSuccess {
            // Log the action
            logRepository.addLog(
                ActionLog(
                    userId = currentUser.id,
                    userName = currentUser.name,
                    userRole = currentUser.role,
                    timestamp = LocalDateTime.now(),
                    actionType = ActionType.MANUAL_OVERRIDE,
                    description = "Changed ${direction.name} light to ${phase.name}",
                    previousState = "Phase: ${currentState.currentPhase.name}, Time: ${currentState.remainingTime}s",
                    newState = "Phase: ${phase.name}"
                )
            )
        }

        return result
    }
}