package com.example.trafficlightcontrol.ui.viewmodel.operator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trafficlightcontrol.domain.model.ActionLog
import com.example.trafficlightcontrol.domain.repo.LogRepository
import com.example.trafficlightcontrol.domain.repo.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OperatorHistoryViewModel @Inject constructor(
    private val logRepository: LogRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    var state by mutableStateOf(OperatorHistoryState())
        private set

    init {
        loadLogs()
    }

    private fun loadLogs() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val currentUser = userRepository.getCurrentUser().first()
            if (currentUser == null) {
                state = state.copy(
                    error = "User not authenticated",
                    isLoading = false
                )
                return@launch
            }

            logRepository.getLogsByUser(currentUser.id)
                .onSuccess { logs ->
                    state = state.copy(
                        logs = logs,
                        isLoading = false
                    )
                }
                .onFailure {
                    state = state.copy(
                        error = it.message ?: "Failed to load logs",
                        isLoading = false
                    )
                }
        }
    }
}

data class OperatorHistoryState(
    val isLoading: Boolean = false,
    val logs: List<ActionLog> = emptyList(),
    val error: String? = null
)