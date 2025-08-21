package com.example.trafficlightcontrol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trafficlightcontrol.data.model.*
import com.example.trafficlightcontrol.data.repository.TrafficLightRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class HistoryViewModel(
    private val repository: TrafficLightRepository
) : ViewModel() {

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Bộ lọc
    private val _selectedDate = MutableStateFlow<Date?>(null)
    val selectedDate: StateFlow<Date?> = _selectedDate

    init {
        loadLogs()
    }

    private fun loadLogs() {
        viewModelScope.launch {
            try {
                repository.getLogs(100).collect { logEntries ->
                    _logs.value = filterLogsByDate(logEntries)
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Lỗi khi tải lịch sử: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun setDateFilter(date: Date?) {
        _selectedDate.value = date
        // Áp dụng bộ lọc cho dữ liệu hiện tại
        _logs.value = filterLogsByDate(_logs.value)
    }

    private fun filterLogsByDate(logs: List<LogEntry>): List<LogEntry> {
        val filterDate = _selectedDate.value ?: return logs

        return logs.filter { log ->
            val logDate = Date(log.timestamp)
            isSameDay(logDate, filterDate)
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    fun clearError() {
        _error.value = null
    }
}