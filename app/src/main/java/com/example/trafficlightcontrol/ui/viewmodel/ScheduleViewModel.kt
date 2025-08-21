package com.example.trafficlightcontrol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trafficlightcontrol.data.model.*
import com.example.trafficlightcontrol.data.repository.TrafficLightRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class ScheduleViewModel(
    private val repository: TrafficLightRepository
) : ViewModel() {

    private val _scheduleEntries = MutableStateFlow<List<ScheduleEntry>>(emptyList())
    val scheduleEntries: StateFlow<List<ScheduleEntry>> = _scheduleEntries

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Trạng thái cho form thêm/chỉnh sửa
    private val _currentEditingEntry = MutableStateFlow<ScheduleEntry?>(null)
    val currentEditingEntry: StateFlow<ScheduleEntry?> = _currentEditingEntry

    init {
        loadSchedule()
    }

    private fun loadSchedule() {
        viewModelScope.launch {
            try {
                repository.getSchedule().collect { entries ->
                    // Sắp xếp lịch theo thời gian bắt đầu
                    val sortedEntries = entries.sortedBy { it.start_time }
                    _scheduleEntries.value = sortedEntries
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Lỗi khi tải lịch: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun startEditing(entry: ScheduleEntry?) {
        _currentEditingEntry.value = entry
    }

    fun cancelEditing() {
        _currentEditingEntry.value = null
    }

    fun saveScheduleEntry(entry: ScheduleEntry) {
        viewModelScope.launch {
            try {
                if (entry.id.isEmpty()) {
                    // Thêm mới
                    repository.addScheduleEntry(entry)
                } else {
                    // Cập nhật
                    repository.updateScheduleEntry(entry)
                }
                _currentEditingEntry.value = null
            } catch (e: Exception) {
                _error.value = "Lỗi khi lưu lịch: ${e.message}"
            }
        }
    }

    fun deleteScheduleEntry(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteScheduleEntry(id)
            } catch (e: Exception) {
                _error.value = "Lỗi khi xóa lịch: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}