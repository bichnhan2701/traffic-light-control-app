package com.example.trafficlightcontrol.data

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.annotation.RequiresPermission
import java.util.UUID

class BluetoothDeviceWrapper(private val device: BluetoothDevice) {
    companion object {
        private const val TAG = "BluetoothDeviceWrapper"
    }

    val address: String
        get() {
            return try {
                device.address
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException getting device address", e)
                "Unknown"
            }
        }

    val name: String
        get() {
            return try {
                device.name ?: "Unknown device"
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException getting device name", e)
                "Unknown device"
            }
        }

    val displayName: String
        get() = "$name ($address)"

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun createRfcommSocketToServiceRecord(uuid: UUID) =
        device.createRfcommSocketToServiceRecord(uuid)

    // Các phương thức khác của BluetoothDevice có thể được thêm vào đây nếu cần
}

// Phương thức mở rộng để dễ dàng chuyển đổi
fun BluetoothDevice.toWrapper(): BluetoothDeviceWrapper {
    return BluetoothDeviceWrapper(this)
}

fun List<BluetoothDevice>.toWrappers(): List<BluetoothDeviceWrapper> {
    return this.map { it.toWrapper() }
}