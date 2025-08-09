package com.example.trafficlightcontrol.ui.screen.operator

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trafficlightcontrol.data.remote.BluetoothService
import com.example.trafficlightcontrol.domain.model.Direction
import com.example.trafficlightcontrol.domain.model.LightPhase
import com.example.trafficlightcontrol.ui.component.TrafficLightDisplay
import com.example.trafficlightcontrol.ui.viewmodel.BluetoothDeviceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorTrafficStatusScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToBluetooth: () -> Unit,
    onLogout: () -> Unit,
    bluetoothViewModel: BluetoothDeviceViewModel = hiltViewModel()
) {
    val state by bluetoothViewModel.state.collectAsState()
    var showManualOverrideDialog by remember { mutableStateOf(false) }
    var selectedDirection by remember { mutableStateOf(Direction.NORTH_SOUTH) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Điều khiển đèn giao thông") },
                actions = {
                    // Hiển thị trạng thái kết nối Bluetooth
                    when (state.connectionState) {
                        BluetoothService.ConnectionState.CONNECTED -> {
                            Icon(
                                imageVector = Icons.Default.BluetoothConnected,
                                contentDescription = "Đã kết nối",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        BluetoothService.ConnectionState.CONNECTING -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        else -> {
                            IconButton(onClick = onNavigateToBluetooth) {
                                Icon(
                                    imageVector = Icons.Default.BluetoothDisabled,
                                    contentDescription = "Kết nối Bluetooth",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "Lịch sử")
                    }

                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Đăng xuất")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.connectionState != BluetoothService.ConnectionState.CONNECTED) {
                // Hiển thị thông báo nếu chưa kết nối
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BluetoothSearching,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Chưa kết nối với hệ thống đèn giao thông",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = onNavigateToBluetooth) {
                            Text("Kết nối qua Bluetooth")
                        }
                    }
                }
            } else {
                // Hiển thị trạng thái đèn giao thông
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Đèn Bắc-Nam
                    state.northSouthState?.let { lightState ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            TrafficLightDisplay(
                                direction = Direction.NORTH_SOUTH,
                                currentPhase = lightState.currentPhase,
                                remainingTime = lightState.remainingTime,
                                isManualOverride = lightState.isManualOverride
                            )

                            Button(
                                onClick = {
                                    selectedDirection = Direction.NORTH_SOUTH
                                    showManualOverrideDialog = true
                                },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Điều khiển thủ công")
                            }
                        }
                    }

                    // Đèn Đông-Tây
                    state.eastWestState?.let { lightState ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            TrafficLightDisplay(
                                direction = Direction.EAST_WEST,
                                currentPhase = lightState.currentPhase,
                                remainingTime = lightState.remainingTime,
                                isManualOverride = lightState.isManualOverride
                            )

                            Button(
                                onClick = {
                                    selectedDirection = Direction.EAST_WEST
                                    showManualOverrideDialog = true
                                },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Điều khiển thủ công")
                            }
                        }
                    }
                }

                // Nút tắt chế độ điều khiển thủ công
                if (state.northSouthState?.isManualOverride == true || state.eastWestState?.isManualOverride == true) {
                    Button(
                        onClick = { bluetoothViewModel.resetManualOverride() },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Trở về chế độ tự động")
                    }
                }
            }
        }
    }

    // Hộp thoại điều khiển thủ công
    if (showManualOverrideDialog) {
        AlertDialog(
            onDismissRequest = { showManualOverrideDialog = false },
            title = { Text("Điều khiển thủ công") },
            text = {
                Column {
                    Text("Chọn trạng thái đèn cho hướng ${
                        when (selectedDirection) {
                            Direction.NORTH_SOUTH -> "Bắc-Nam"
                            Direction.EAST_WEST -> "Đông-Tây"
                        }
                    }:")

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                bluetoothViewModel.changePhase(selectedDirection, LightPhase.RED)
                                showManualOverrideDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("ĐỎ")
                        }

                        Button(
                            onClick = {
                                bluetoothViewModel.changePhase(selectedDirection, LightPhase.YELLOW)
                                showManualOverrideDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Text("VÀNG")
                        }

                        Button(
                            onClick = {
                                bluetoothViewModel.changePhase(selectedDirection, LightPhase.GREEN)
                                showManualOverrideDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("XANH")
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = {
                TextButton(onClick = { showManualOverrideDialog = false }) {
                    Text("ĐÓNG")
                }
            }
        )
    }
}