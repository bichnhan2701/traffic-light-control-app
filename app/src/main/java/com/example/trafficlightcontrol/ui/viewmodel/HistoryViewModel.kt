package com.example.trafficlightcontrol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trafficlightcontrol.data.model.LogEntry
import com.example.trafficlightcontrol.data.repository.TrafficLightRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*
import java.time.ZoneId

data class HistoryUiState(
    val isLoading: Boolean = true,
    val dateFilter: LocalDate? = null,
    val items: List<LogEntry> = emptyList()
)

class HistoryViewModel(
    private val repo: TrafficLightRepository,
    private val intersectionId: String
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init {
        observeLogs()
    }

    private fun observeLogs() {
        viewModelScope.launch {
            repo.logsFlow(intersectionId, limit = 200)
                .onStart { _state.update { it.copy(isLoading = true) } }
                .collect { logs ->
                    _state.update { cur ->
                        cur.copy(
                            isLoading = false,
                            items = applyDateFilter(logs, cur.dateFilter)
                        )
                    }
                }
        }
    }

    fun setDateFilter(date: LocalDate?) {
        _state.update { cur ->
            cur.copy(
                dateFilter = date,
                items = applyDateFilter(cur.items, date, recheck = true)
            )
        }
    }

    fun clearFilter() = setDateFilter(null)

    private fun applyDateFilter(
        logs: List<LogEntry>,
        date: LocalDate?,
        recheck: Boolean = false
    ): List<LogEntry> {
        if (date == null) return logs
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        return logs.filter { it.ts in start..end }
            .sortedByDescending { it.ts }
    }
}