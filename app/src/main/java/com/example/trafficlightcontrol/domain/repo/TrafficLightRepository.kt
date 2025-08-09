package com.example.trafficlightcontrol.domain.repo

import com.example.trafficlightcontrol.domain.model.Direction
import com.example.trafficlightcontrol.domain.model.LightPhase
import com.example.trafficlightcontrol.domain.model.PhaseConfiguration
import com.example.trafficlightcontrol.domain.model.TrafficLightState
import kotlinx.coroutines.flow.Flow

interface TrafficLightRepository {
    fun observeTrafficLightState(direction: Direction): Flow<TrafficLightState>
    suspend fun changePhase(direction: Direction, phase: LightPhase): Result<Unit>
    suspend fun resetPhaseTimer(direction: Direction): Result<Unit>
    suspend fun setManualOverride(direction: Direction, override: Boolean): Result<Unit>

    suspend fun getPhaseConfigurations(): Result<List<PhaseConfiguration>>
    suspend fun savePhaseConfiguration(config: PhaseConfiguration): Result<Unit>
    suspend fun deletePhaseConfiguration(configId: String): Result<Unit>
}