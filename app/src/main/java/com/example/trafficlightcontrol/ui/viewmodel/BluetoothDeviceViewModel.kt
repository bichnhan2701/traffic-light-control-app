package com.example.trafficlightcontrol.ui.viewmodel

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trafficlightcontrol.data.remote.BluetoothService
import com.example.trafficlightcontrol.domain.model.Direction
import com.example.trafficlightcontrol.domain.model.LightPhase
import com.example.trafficlightcontrol.domain.model.TrafficLightState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothDeviceViewModel @Inject constructor(
    private val bluetoothService: BluetoothService
) : ViewModel() {

    private val _state = MutableStateFlow(BluetoothState())
    val state: StateFlow<BluetoothState> = _state.asStateFlow()

    init {
        checkBluetoothAvailability()

        viewModelScope.launch {
            bluetoothService.connectionState.collect { connectionState ->
                val permissionDenied = connectionState == BluetoothService.ConnectionState.PERMISSION_DENIED

                _state.value = _state.value.copy(
                    connectionState = connectionState,
                    permissionDenied = permissionDenied
                )
            }
        }

        viewModelScope.launch {
            bluetoothService.northSouthState.collect { northSouthState ->
                _state.value = _state.value.copy(
                    northSouthState = northSouthState
                )
            }
        }

        viewModelScope.launch {
            bluetoothService.eastWestState.collect { eastWestState ->
                _state.value = _state.value.copy(
                    eastWestState = eastWestState
                )
            }
        }
    }

    private fun checkBluetoothAvailability() {
        val isAvailable = bluetoothService.isBluetoothAvailable()
        val isEnabled = bluetoothService.isBluetoothEnabled()

        _state.value = _state.value.copy(
            isBluetoothAvailable = isAvailable,
            isBluetoothEnabled = isEnabled
        )

        if (isAvailable && isEnabled) {
            loadPairedDevices()
        }
    }

    fun loadPairedDevices() {
        val devices = bluetoothService.getPairedDevices()
        _state.value = _state.value.copy(
            pairedDevices = devices
        )
    }

    fun connectToDevice(device: BluetoothDevice) {
        // Trích xuất thông tin thiết bị một cách an toàn
        val deviceInfo = getDeviceInfo(device)

        _state.value = _state.value.copy(
            connectingDeviceAddress = deviceInfo.address,
            connectionError = null
        )

        val success = bluetoothService.connect(device)

        if (!success) {
            _state.value = _state.value.copy(
                connectingDeviceAddress = null,
                connectionError = if (_state.value.permissionDenied)
                    "Cần quyền truy cập Bluetooth"
                else
                    "Không thể kết nối với ${deviceInfo.displayName}"
            )
        } else {
            _state.value = _state.value.copy(
                connectingDeviceAddress = null
            )
        }
    }

    // Phương thức an toàn để lấy thông tin thiết bị
    private fun getDeviceInfo(device: BluetoothDevice): DeviceInfo {
        val address = try {
            device.address
        } catch (e: SecurityException) {
            "Thiết bị không xác định"
        }

        val name = try {
            device.name ?: "Không xác định"
        } catch (e: SecurityException) {
            "Không xác định"
        }

        return DeviceInfo(address, name)
    }

    fun getRequiredPermissions(): Array<String> {
        return bluetoothService.getRequiredPermissions()
    }

    fun disconnect() {
        bluetoothService.disconnect()
    }

    fun changePhase(direction: Direction, phase: LightPhase) {
        bluetoothService.changePhase(direction, phase)
    }

    fun resetManualOverride() {
        bluetoothService.resetManualOverride()
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothService.disconnect()
    }
}

// Lớp để lưu trữ thông tin thiết bị một cách an toàn
data class DeviceInfo(
    val address: String,
    val name: String
) {
    val displayName: String
        get() = if (name != "Không xác định") name else address
}

data class BluetoothState(
    val isBluetoothAvailable: Boolean = false,
    val isBluetoothEnabled: Boolean = false,
    val connectionState: BluetoothService.ConnectionState = BluetoothService.ConnectionState.NONE,
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val connectingDeviceAddress: String? = null,
    val connectionError: String? = null,
    val permissionDenied: Boolean = false,
    val northSouthState: TrafficLightState? = null,
    val eastWestState: TrafficLightState? = null
)