package com.example.trafficlightcontrol.ui.screen

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trafficlightcontrol.data.remote.BluetoothService
import com.example.trafficlightcontrol.ui.viewmodel.BluetoothDeviceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothDeviceScreen(
    onNavigateBack: () -> Unit,
    onConnectionEstablished: () -> Unit,
    viewModel: BluetoothDeviceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Request permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            viewModel.loadPairedDevices()
        }
    }

    // Yêu cầu quyền khi màn hình được hiển thị
    LaunchedEffect(Unit) {
        permissionLauncher.launch(viewModel.getRequiredPermissions())
    }

    // Chuyển hướng khi kết nối thành công
    LaunchedEffect(state.connectionState) {
        if (state.connectionState == BluetoothService.ConnectionState.CONNECTED) {
            onConnectionEstablished()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kết nối thiết bị Bluetooth") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    // Nút yêu cầu quyền
                    if (state.permissionDenied) {
                        IconButton(onClick = {
                            permissionLauncher.launch(viewModel.getRequiredPermissions())
                        }) {
                            Icon(
                                Icons.Default.PermDeviceInformation,
                                contentDescription = "Yêu cầu quyền",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (state.permissionDenied) {
                // Hiển thị thông báo yêu cầu quyền
                InfoCard(
                    icon = Icons.Default.PermDeviceInformation,
                    title = "Cần quyền truy cập",
                    description = "Ứng dụng cần quyền truy cập Bluetooth để kết nối với thiết bị.",
                    buttonText = "Cấp quyền",
                    onClick = {
                        permissionLauncher.launch(viewModel.getRequiredPermissions())
                    }
                )
            } else if (!state.isBluetoothAvailable) {
                // Hiển thị thông báo nếu thiết bị không hỗ trợ Bluetooth
                InfoCard(
                    icon = Icons.Default.BluetoothDisabled,
                    title = "Bluetooth không khả dụng",
                    description = "Thiết bị của bạn không hỗ trợ Bluetooth",
                    buttonText = null,
                    onClick = null
                )
            } else if (!state.isBluetoothEnabled) {
                // Hiển thị thông báo nếu Bluetooth chưa được bật
                InfoCard(
                    icon = Icons.Default.SettingsBluetooth,
                    title = "Bluetooth đang tắt",
                    description = "Vui lòng bật Bluetooth để tiếp tục",
                    buttonText = "Bật Bluetooth",
                    onClick = {
                        val enableBtIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                        context.startActivity(enableBtIntent)
                    }
                )
            } else {
                // Hiển thị danh sách thiết bị đã ghép đôi
                Text(
                    text = "Chọn thiết bị để kết nối",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (state.pairedDevices.isEmpty()) {
                    // Hiển thị thông báo nếu không có thiết bị đã ghép đôi
                    InfoCard(
                        icon = Icons.Default.Devices,
                        title = "Không tìm thấy thiết bị",
                        description = "Hãy ghép đôi HC-05 trong cài đặt Bluetooth trước",
                        buttonText = "Mở cài đặt Bluetooth",
                        onClick = {
                            val btSettingsIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                            context.startActivity(btSettingsIntent)
                        }
                    )
                } else {
                    // Hiển thị danh sách thiết bị
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(state.pairedDevices) { device ->
                            DeviceItem(
                                device = device,
                                isConnecting = device.address == state.connectingDeviceAddress,
                                onClick = { viewModel.connectToDevice(device) }
                            )
                        }
                    }
                }

                // Hiển thị lỗi nếu có
                if (state.connectionError != null) {
                    Text(
                        text = state.connectionError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    buttonText: String?,
    onClick: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .padding(bottom = 8.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            if (buttonText != null && onClick != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onClick) {
                    Text(buttonText)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceItem(
    device: BluetoothDevice,
    isConnecting: Boolean,
    onClick: () -> Unit
) {
    // Lấy thông tin thiết bị một cách an toàn
    val deviceName = remember {
        try {
            device.name ?: "Thiết bị không xác định"
        } catch (e: SecurityException) {
            "Thiết bị không xác định"
        }
    }

    val deviceAddress = remember {
        try {
            device.address
        } catch (e: SecurityException) {
            "Địa chỉ không xác định"
        }
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Bluetooth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = deviceName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = deviceAddress,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Kết nối"
                )
            }
        }
    }
}