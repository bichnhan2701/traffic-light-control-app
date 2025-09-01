package com.example.trafficlightcontrol.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trafficlightcontrol.data.model.*
import com.example.trafficlightcontrol.data.repository.TrafficLightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ControlViewModel(
    private val repo: TrafficLightRepository,
    private val intersectionId: String
) : ViewModel() {

    companion object {
        private const val GREEN_MIN_S = 5
        private const val GREEN_MAX_S = 180
    }
    private fun clampGreen(v: Int) = v.coerceIn(GREEN_MIN_S, GREEN_MAX_S)

    // ==== state gửi lệnh ====
    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending

    private val _lastAck = MutableStateFlow<Ack?>(null)
    val lastAck: StateFlow<Ack?> = _lastAck

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _message = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val message: SharedFlow<String> = _message

    fun clearError() { _error.value = null }

    // ==== state HIỆN HÀNH từ /reported ====
    // Lấy luôn Reported để UI có thể hiển thị đúng mode, emergency, peak...
    private val reportedFlow: Flow<Reported> = repo.reportedFlow(intersectionId)

    val currentMode: StateFlow<Mode> =
        reportedFlow.map { it.mode }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Mode.default)

    val currentEmergencyPriority: StateFlow<String?> =
        reportedFlow.map { it.emergencyPriority }  // "A"|"B"|null
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Thời gian peak đang áp dụng (nếu mode==peak sẽ có, còn lại có thể null)
    val currentPeakA: StateFlow<Int?> =
        reportedFlow.map { it.peakGreenA_s }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentPeakB: StateFlow<Int?> =
        reportedFlow.map { it.peakGreenB_s }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // ==== Actions ====
    fun setDefault() = send("Đặt chế độ mặc định") {
        repo.setDefault(intersectionId)
    }
    fun setNight() = send("Đặt chế độ đêm") {
        repo.setNight(intersectionId)
    }
    fun setEmergencyA() = send("Bật khẩn cấp A") {
        repo.setEmergency(intersectionId, "A")
    }
    fun setEmergencyB() = send("Bật khẩn cấp B") {
        repo.setEmergency(intersectionId, "B")
    }
    fun setPeak(greenA_s: Int, greenB_s: Int) = send("Đặt giờ cao điểm A=$greenA_s B=$greenB_s") {
        repo.setPeak(intersectionId, clampGreen(greenA_s), clampGreen(greenB_s))
    }

    private inline fun send(title: String, crossinline block: suspend () -> Ack?) {
        viewModelScope.launch {
            if (_isSending.value) return@launch
            _isSending.value = true
            _error.value = null
            try {
                val ack = block()
                _lastAck.value = ack
                val msg = when (ack?.status) {
                    "applied"  -> "$title: OK"
                    "rejected" -> "$title: Bị từ chối${ack.reason?.let { " ($it)" } ?: ""}"
                    else       -> "$title: Chưa nhận ack (sẽ đồng bộ theo /reported)"
                }
                _message.tryEmit(msg)
            } catch (t: Throwable) {
                _error.value = t.message ?: "Lỗi không xác định"
            } finally {
                _isSending.value = false
            }
        }
    }
}
