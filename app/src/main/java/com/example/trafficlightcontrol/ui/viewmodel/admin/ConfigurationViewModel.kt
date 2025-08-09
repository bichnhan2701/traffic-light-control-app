package com.example.trafficlightcontrol.ui.viewmodel.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trafficlightcontrol.domain.model.PhaseConfiguration
import com.example.trafficlightcontrol.domain.repo.TrafficLightRepository
import com.example.trafficlightcontrol.domain.usecase.SavePhaseConfigurationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfigurationViewModel @Inject constructor(
    private val trafficLightRepository: TrafficLightRepository,
    private val savePhaseConfigurationUseCase: SavePhaseConfigurationUseCase
) : ViewModel() {

    var state by mutableStateOf(ConfigurationState())
        private set

    init {
        loadConfigurations()
    }

    private fun loadConfigurations() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            trafficLightRepository.getPhaseConfigurations()
                .onSuccess { configurations ->
                    state = state.copy(
                        configurations = configurations.sortedBy { it.timeRange.start },
                        isLoading = false
                    )
                }
                .onFailure {
                    state = state.copy(
                        error = it.message ?: "Failed to load configurations",
                        isLoading = false
                    )
                }
        }
    }

    fun saveConfiguration(configuration: PhaseConfiguration) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            savePhaseConfigurationUseCase(configuration)
                .onSuccess {
                    loadConfigurations()
                }
                .onFailure {
                    state = state.copy(
                        error = it.message ?: "Failed to save configuration",
                        isLoading = false
                    )
                }
        }
    }

    fun deleteConfiguration(configId: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            trafficLightRepository.deletePhaseConfiguration(configId)
                .onSuccess {
                    loadConfigurations()
                }
                .onFailure {
                    state = state.copy(
                        error = it.message ?: "Failed to delete configuration",
                        isLoading = false
                    )
                }
        }
    }
}

data class ConfigurationState(
    val isLoading: Boolean = false,
    val configurations: List<PhaseConfiguration> = emptyList(),
    val error: String? = null
)