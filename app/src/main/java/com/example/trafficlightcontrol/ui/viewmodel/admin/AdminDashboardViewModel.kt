package com.example.trafficlightcontrol.ui.viewmodel.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trafficlightcontrol.domain.model.ActionLog
import com.example.trafficlightcontrol.domain.model.Direction
import com.example.trafficlightcontrol.domain.model.LightPhase
import com.example.trafficlightcontrol.domain.model.PhaseConfiguration
import com.example.trafficlightcontrol.domain.model.TrafficLightState
import com.example.trafficlightcontrol.domain.repo.LogRepository
import com.example.trafficlightcontrol.domain.repo.TrafficLightRepository
import com.example.trafficlightcontrol.domain.usecase.ChangePhaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val trafficLightRepository: TrafficLightRepository,
    private val logRepository: LogRepository,
    private val changePhaseUseCase: ChangePhaseUseCase
) : ViewModel() {

    var state by mutableStateOf(AdminDashboardState())
        private set

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            // Observe North-South traffic light
            launch {
                trafficLightRepository.observeTrafficLightState(Direction.NORTH_SOUTH)
                    .collectLatest { lightState ->
                        state = state.copy(northSouthState = lightState)
                    }
            }

            // Observe East-West traffic light
            launch {
                trafficLightRepository.observeTrafficLightState(Direction.EAST_WEST)
                    .collectLatest { lightState ->
                        state = state.copy(eastWestState = lightState)
                    }
            }

            // Load phase configurations
            launch {
                trafficLightRepository.getPhaseConfigurations()
                    .onSuccess { configs ->
                        val now = LocalTime.now()
                        val activeConfig = configs.find { config ->
                            now.isAfter(config.timeRange.start) && now.isBefore(config.timeRange.end)
                        }
                        state = state.copy(
                            configurations = configs,
                            activeConfiguration = activeConfig
                        )
                    }
            }

            // Observe recent logs
            launch {
                logRepository.observeRecentLogs()
                    .collectLatest { logs ->
                        state = state.copy(recentLogs = logs)
                    }
            }

            state = state.copy(isLoading = false)
        }
    }

    fun changePhase(direction: Direction, phase: LightPhase) {
        viewModelScope.launch {
            changePhaseUseCase(direction, phase)
        }
    }

    fun resetManualOverride(direction: Direction) {
        viewModelScope.launch {
            trafficLightRepository.setManualOverride(direction, false)
        }
    }
}

data class AdminDashboardState(
    val isLoading: Boolean = false,
    val northSouthState: TrafficLightState? = null,
    val eastWestState: TrafficLightState? = null,
    val configurations: List<PhaseConfiguration> = emptyList(),
    val activeConfiguration: PhaseConfiguration? = null,
    val recentLogs: List<ActionLog> = emptyList()
)