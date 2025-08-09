package com.example.trafficlightcontrol.ui.viewmodel.operator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trafficlightcontrol.domain.model.Direction
import com.example.trafficlightcontrol.domain.model.LightPhase
import com.example.trafficlightcontrol.domain.model.TrafficLightState
import com.example.trafficlightcontrol.domain.repo.TrafficLightRepository
import com.example.trafficlightcontrol.domain.usecase.ChangePhaseUseCase
import com.example.trafficlightcontrol.domain.usecase.ResetPhaseTimerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OperatorTrafficStatusViewModel @Inject constructor(
    private val trafficLightRepository: TrafficLightRepository,
    private val changePhaseUseCase: ChangePhaseUseCase,
    private val resetPhaseTimerUseCase: ResetPhaseTimerUseCase
) : ViewModel() {

    var state by mutableStateOf(OperatorTrafficStatusState())
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

            state = state.copy(isLoading = false)
        }
    }

    fun changePhase(direction: Direction, phase: LightPhase) {
        viewModelScope.launch {
            changePhaseUseCase(direction, phase)
                .onFailure {
                    state = state.copy(error = it.message ?: "Failed to change phase")
                }
        }
    }

    fun resetPhaseTimer(direction: Direction) {
        viewModelScope.launch {
            resetPhaseTimerUseCase(direction)
                .onFailure {
                    state = state.copy(error = it.message ?: "Failed to reset timer")
                }
        }
    }

    fun resetManualOverride(direction: Direction) {
        viewModelScope.launch {
            trafficLightRepository.setManualOverride(direction, false)
                .onFailure {
                    state = state.copy(error = it.message ?: "Failed to reset manual override")
                }
        }
    }
}

data class OperatorTrafficStatusState(
    val isLoading: Boolean = false,
    val northSouthState: TrafficLightState? = null,
    val eastWestState: TrafficLightState? = null,
    val error: String? = null
)