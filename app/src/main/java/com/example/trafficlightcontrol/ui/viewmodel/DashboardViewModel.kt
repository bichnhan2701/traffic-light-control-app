package com.example.trafficlightcontrol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trafficlightcontrol.data.model.*
import com.example.trafficlightcontrol.data.repository.TrafficLightRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: TrafficLightRepository
) : ViewModel() {

    private val _currentStatus = MutableStateFlow<TrafficLightCurrent?>(null)
    val currentStatus: StateFlow<TrafficLightCurrent?> = _currentStatus

    private val _systemStatus = MutableStateFlow<SystemStatus?>(null)
    val systemStatus: StateFlow<SystemStatus?> = _systemStatus

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                // Lắng nghe trạng thái hiện tại
                launch {
                    repository.getCurrentStatus().collect { current ->
                        _currentStatus.value = current
                        _isLoading.value = false
                    }
                }

                // Lắng nghe trạng thái hệ thống
                launch {
                    repository.getSystemStatus().collect { system ->
                        _systemStatus.value = system
                    }
                }
            } catch (e: Exception) {
                _error.value = "Lỗi khi tải dữ liệu: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun changeMode(mode: String, username: String) {
        viewModelScope.launch {
            try {
                repository.changeMode(mode, username)
            } catch (e: Exception) {
                _error.value = "Lỗi khi thay đổi chế độ: ${e.message}"
            }
        }
    }

    fun activateEmergency(direction: String, username: String) {
        viewModelScope.launch {
            try {
                repository.activateEmergency(direction, username)
            } catch (e: Exception) {
                _error.value = "Lỗi khi kích hoạt chế độ khẩn cấp: ${e.message}"
            }
        }
    }

    fun deactivateEmergency(username: String) {
        viewModelScope.launch {
            try {
                repository.deactivateEmergency(username)
            } catch (e: Exception) {
                _error.value = "Lỗi khi kết thúc chế độ khẩn cấp: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun syncTime() {
        viewModelScope.launch {
            try {
                repository.syncTime()
            } catch (e: Exception) {
                _error.value = "Lỗi khi đồng bộ thời gian: ${e.message}"
            }
        }
    }
}