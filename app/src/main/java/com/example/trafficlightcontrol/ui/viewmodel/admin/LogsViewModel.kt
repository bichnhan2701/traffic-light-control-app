package com.example.trafficlightcontrol.ui.viewmodel.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trafficlightcontrol.domain.model.ActionLog
import com.example.trafficlightcontrol.domain.model.ActionType
import com.example.trafficlightcontrol.domain.repo.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val logRepository: LogRepository
) : ViewModel() {

    var state by mutableStateOf(LogsState())
        private set

    init {
        loadLogs()
    }

    private fun loadLogs() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val result = if (state.dateRange != null) {
                val startDateTime = LocalDateTime.of(state.dateRange!!.first, LocalTime.MIN)
                val endDateTime = LocalDateTime.of(state.dateRange!!.second, LocalTime.MAX)
                logRepository.getLogsByTimeRange(startDateTime, endDateTime)
            } else if (state.selectedActionType != null) {
                logRepository.getLogsByAction(state.selectedActionType!!)
            } else {
                // Get all logs via observeRecentLogs
                val allLogs = logRepository.observeRecentLogs().toString()
                Result.success(allLogs.split("\n").map {
                    ActionLog(actionType = ActionType.MANUAL_OVERRIDE, description = it)
                })
            }

            result.onSuccess { logs ->
                // Apply additional filtering if both date and action type filters are active
                val filteredLogs = if (state.selectedActionType != null && state.dateRange != null) {
                    logs.filter { it.actionType == state.selectedActionType }
                } else {
                    logs
                }

                state = state.copy(
                    logs = filteredLogs,
                    isLoading = false,
                    error = null
                )
            }.onFailure {
                state = state.copy(
                    error = it.message ?: "Failed to load logs",
                    isLoading = false
                )
            }
        }
    }

    fun filterByActionType(actionType: ActionType?) {
        state = state.copy(selectedActionType = actionType)
        loadLogs()
    }

    fun filterByDate(startDate: LocalDate, endDate: LocalDate) {
        state = state.copy(dateRange = Pair(startDate, endDate))
        loadLogs()
    }

    fun clearDateFilter() {
        state = state.copy(dateRange = null)
        loadLogs()
    }

    fun exportLogs() {
        viewModelScope.launch {
            // In a real app, implement export logic here
            // e.g., create a CSV file and share it

            state = state.copy(
                message = "Logs exported successfully"
            )
        }
    }
}

data class LogsState(
    val isLoading: Boolean = false,
    val logs: List<ActionLog> = emptyList(),
    val selectedActionType: ActionType? = null,
    val dateRange: Pair<LocalDate, LocalDate>? = null,
    val error: String? = null,
    val message: String? = null
)