package com.example.trafficlightcontrol.domain.model

enum class ConnectionType {
    MQTT,
    USB_SERIAL,
    SOCKET_IO,
    BLUETOOTH  // Thêm loại kết nối Bluetooth
}