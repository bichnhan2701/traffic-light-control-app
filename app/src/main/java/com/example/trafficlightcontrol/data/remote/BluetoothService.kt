package com.example.trafficlightcontrol.data.remote

import com.example.trafficlightcontrol.domain.model.Direction
import com.example.trafficlightcontrol.domain.model.LightPhase
import com.example.trafficlightcontrol.domain.model.TrafficLightState
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.trafficlightcontrol.util.PermissionHelper
import com.google.gson.JsonParser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "BluetoothService"

        // UUID tiêu chuẩn cho SPP (Serial Port Profile)
        private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    // Các trạng thái kết nối
    enum class ConnectionState { NONE, CONNECTING, CONNECTED, PERMISSION_DENIED }

    // Các thành phần Bluetooth
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        if (hasBluetoothPermissions()) {
            BluetoothAdapter.getDefaultAdapter()
        } else {
            null
        }
    }
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    // Quản lý quyền
    private val permissionHelper = PermissionHelper(context)

    // Các luồng dữ liệu
    private val _connectionState = MutableStateFlow(ConnectionState.NONE)
    val connectionState: Flow<ConnectionState> = _connectionState

    private val _northSouthState = MutableStateFlow(
        TrafficLightState(
            id = "north_south",
            direction = Direction.NORTH_SOUTH,
            currentPhase = LightPhase.RED,
            remainingTime = 60,
            isManualOverride = false
        )
    )
    val northSouthState: Flow<TrafficLightState> = _northSouthState

    private val _eastWestState = MutableStateFlow(
        TrafficLightState(
            id = "east_west",
            direction = Direction.EAST_WEST,
            currentPhase = LightPhase.GREEN,
            remainingTime = 55,
            isManualOverride = false
        )
    )
    val eastWestState: Flow<TrafficLightState> = _eastWestState

    // Thread đọc dữ liệu
    private var readThread: Thread? = null
    private val handler = Handler(Looper.getMainLooper())

    // Kiểm tra quyền Bluetooth
    private fun hasBluetoothPermissions(): Boolean {
        return permissionHelper.hasBluetoothPermissions()
    }

    // Kiểm tra Bluetooth có sẵn không
    fun isBluetoothAvailable(): Boolean {
        if (!hasBluetoothPermissions()) {
            return false
        }
        return try {
            BluetoothAdapter.getDefaultAdapter() != null
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception checking Bluetooth availability", e)
            false
        }
    }

    // Kiểm tra Bluetooth đã bật chưa
    fun isBluetoothEnabled(): Boolean {
        if (!hasBluetoothPermissions()) {
            return false
        }
        return try {
            bluetoothAdapter?.isEnabled == true
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception checking if Bluetooth is enabled", e)
            false
        }
    }

    // Lấy danh sách thiết bị đã ghép đôi
    fun getPairedDevices(): List<BluetoothDevice> {
        if (!hasBluetoothPermissions()) {
            return emptyList()
        }
        return try {
            bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception getting paired devices", e)
            emptyList()
        }
    }

    // Kết nối đến thiết bị Bluetooth
    // Sửa phương thức connect() để xử lý SecurityException khi lấy thông tin thiết bị
    fun connect(device: BluetoothDevice): Boolean {
        // Kiểm tra quyền trước khi kết nối
        if (!hasBluetoothPermissions()) {
            _connectionState.value = ConnectionState.PERMISSION_DENIED
            return false
        }

        // Lưu thông tin thiết bị một cách an toàn để ghi log
        val deviceInfo = try {
            "${device.name ?: "Unknown"} (${device.address})"
        } catch (e: SecurityException) {
            "Unknown device"
        }

        Log.d(TAG, "Connecting to $deviceInfo")

        // Đảm bảo ngắt kết nối trước khi kết nối mới
        disconnect()

        _connectionState.value = ConnectionState.CONNECTING

        try {
            // Tạo socket kết nối
            bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)

            try {
                bluetoothAdapter?.cancelDiscovery() // Hủy tìm kiếm để tiết kiệm pin
            } catch (e: SecurityException) {
                Log.e(TAG, "Security exception canceling discovery", e)
            }

            // Thực hiện kết nối (có thể mất thời gian)
            bluetoothSocket?.connect()

            // Lấy luồng dữ liệu vào/ra
            inputStream = bluetoothSocket?.inputStream
            outputStream = bluetoothSocket?.outputStream

            // Khởi động thread đọc dữ liệu
            startReadThread()

            _connectionState.value = ConnectionState.CONNECTED
            return true
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception connecting to device", e)
            _connectionState.value = ConnectionState.PERMISSION_DENIED
            disconnect()
            return false
        } catch (e: IOException) {
            Log.e(TAG, "IO exception connecting to device: ${e.message}", e)
            disconnect()
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to device: ${e.message}", e)
            disconnect()
            return false
        }
    }

    // Ngắt kết nối
    fun disconnect() {
        // Dừng thread đọc dữ liệu
        readThread?.interrupt()
        readThread = null

        // Đóng tất cả stream và socket
        try {
            inputStream?.close()
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Lỗi khi ngắt kết nối: ${e.message}", e)
        }

        inputStream = null
        outputStream = null
        bluetoothSocket = null
        _connectionState.value = ConnectionState.NONE
    }

    // Khởi động thread đọc dữ liệu
    private fun startReadThread() {
        readThread = Thread {
            val buffer = ByteArray(1024)
            var bytes: Int

            while (!Thread.currentThread().isInterrupted && _connectionState.value == ConnectionState.CONNECTED) {
                try {
                    // Đọc dữ liệu từ inputStream
                    bytes = inputStream?.read(buffer) ?: -1

                    if (bytes > 0) {
                        // Xử lý dữ liệu nhận được
                        val receivedData = String(buffer, 0, bytes)
                        processReceivedData(receivedData)
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Mất kết nối: ${e.message}", e)
                    handler.post { disconnect() }
                    break
                }
            }
        }

        readThread?.start()
    }

    // Xử lý dữ liệu JSON nhận được từ Arduino
    private fun processReceivedData(data: String) {
        try {
            // Tìm chuỗi JSON hợp lệ
            val jsonStart = data.indexOf("{")
            val jsonEnd = data.lastIndexOf("}") + 1

            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonString = data.substring(jsonStart, jsonEnd)
                val jsonObject = JsonParser.parseString(jsonString).asJsonObject

                // Trạng thái Bắc-Nam
                val nsJson = jsonObject.getAsJsonObject("northSouth")
                val nsPhase = LightPhase.valueOf(nsJson.get("phase").asString)
                val nsTimer = nsJson.get("timer").asInt

                // Trạng thái Đông-Tây
                val ewJson = jsonObject.getAsJsonObject("eastWest")
                val ewPhase = LightPhase.valueOf(ewJson.get("phase").asString)
                val ewTimer = ewJson.get("timer").asInt

                // Chế độ điều khiển
                val manualOverride = jsonObject.get("manualOverride").asBoolean

                // Cập nhật trạng thái
                _northSouthState.value = _northSouthState.value.copy(
                    currentPhase = nsPhase,
                    remainingTime = nsTimer,
                    isManualOverride = manualOverride
                )

                _eastWestState.value = _eastWestState.value.copy(
                    currentPhase = ewPhase,
                    remainingTime = ewTimer,
                    isManualOverride = manualOverride
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi xử lý dữ liệu: $data", e)
        }
    }

    // Gửi lệnh đến Arduino
    fun sendCommand(command: String): Boolean {
        if (_connectionState.value != ConnectionState.CONNECTED) {
            return false
        }

        return try {
            // Thêm ký tự xuống dòng để Arduino nhận diện kết thúc lệnh
            val cmdWithNewline = "$command\n"
            outputStream?.write(cmdWithNewline.toByteArray())
            true
        } catch (e: IOException) {
            Log.e(TAG, "Lỗi gửi lệnh: ${e.message}", e)
            handler.post { disconnect() }
            false
        }
    }

    // Gửi lệnh thay đổi trạng thái đèn
    fun changePhase(direction: Direction, phase: LightPhase): Boolean {
        val command = "${direction.name}:${phase.name}"
        return sendCommand(command)
    }

    // Tắt chế độ điều khiển thủ công
    fun resetManualOverride(): Boolean {
        return sendCommand("RESET")
    }

    // Hàm getter để lấy danh sách quyền cần thiết
    fun getRequiredPermissions(): Array<String> {
        return permissionHelper.getRequiredBluetoothPermissions()
    }
}