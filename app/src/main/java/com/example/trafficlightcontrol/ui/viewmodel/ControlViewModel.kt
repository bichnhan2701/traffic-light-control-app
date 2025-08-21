package com.example.trafficlightcontrol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trafficlightcontrol.data.model.*
import com.example.trafficlightcontrol.data.repository.TrafficLightRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ControlViewModel(
    private val repository: TrafficLightRepository
) : ViewModel() {

    private val _currentStatus = MutableStateFlow<TrafficLightCurrent?>(null)
    val currentStatus: StateFlow<TrafficLightCurrent?> = _currentStatus

    private val _emergencyStatus = MutableStateFlow<EmergencyStatus?>(null)
    val emergencyStatus: StateFlow<EmergencyStatus?> = _emergencyStatus

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

                // Lắng nghe trạng thái khẩn cấp
                launch {
                    repository.getEmergencyStatus().collect { emergency ->
                        _emergencyStatus.value = emergency
                    }
                }
            } catch (e: Exception) {
                _error.value = "Lỗi khi tải dữ liệu: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun activatePeakMode(username: String) {
        viewModelScope.launch {
            try {
                repository.changeMode("peak", username)
            } catch (e: Exception) {
                _error.value = "Lỗi khi kích hoạt chế độ cao điểm: ${e.message}"
            }
        }
    }

    fun activateNightMode(username: String) {
        viewModelScope.launch {
            try {
                repository.changeMode("night", username)
            } catch (e: Exception) {
                _error.value = "Lỗi khi kích hoạt chế độ đêm: ${e.message}"
            }
        }
    }

    fun activateScheduledMode(username: String) {
        viewModelScope.launch {
            try {
                repository.changeMode("scheduled", username)
            } catch (e: Exception) {
                _error.value = "Lỗi khi kích hoạt chế độ theo lịch: ${e.message}"
            }
        }
    }

    fun activateEmergency(direction: String, username: String) {
        viewModelScope.launch {
            try {
                repository.activateEmergency(direction, username)
                // Chuyển sang chế độ khẩn cấp
                repository.changeMode("emergency", username)
            } catch (e: Exception) {
                _error.value = "Lỗi khi kích hoạt chế độ khẩn cấp: ${e.message}"
            }
        }
    }

    fun deactivateEmergency(username: String) {
        viewModelScope.launch {
            try {
                repository.deactivateEmergency(username)
                // Quay lại chế độ theo lịch
                repository.changeMode("scheduled", username)
            } catch (e: Exception) {
                _error.value = "Lỗi khi kết thúc chế độ khẩn cấp: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}