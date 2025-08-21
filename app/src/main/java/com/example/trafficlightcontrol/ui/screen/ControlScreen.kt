package com.example.trafficlightcontrol.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.trafficlightcontrol.data.model.EmergencyStatus
import com.example.trafficlightcontrol.ui.component.ActionButton
import com.example.trafficlightcontrol.ui.component.FlashingYellowLight
import com.example.trafficlightcontrol.ui.component.TrafficLight
import com.example.trafficlightcontrol.ui.theme.*
import com.example.trafficlightcontrol.ui.viewmodel.ControlViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlScreen(
    viewModel: ControlViewModel,
    username: String,
    initialTab: String = "peak"
) {
    val currentStatus by viewModel.currentStatus.collectAsState()
    val emergencyStatus by viewModel.emergencyStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedTab by remember { mutableStateOf(initialTab) }

    val scaffoldState = remember { SnackbarHostState() }

    // Hiển thị Snackbar khi có lỗi
    LaunchedEffect(error) {
        error?.let {
            scaffoldState.showSnackbar(
                message = it,
                actionLabel = "Đóng"
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = scaffoldState) },
        topBar = {
            TopAppBar(
                title = { Text("Điều khiển chi tiết") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab bar
            TabRow(
                selectedTabIndex = when(selectedTab) {
                    "peak" -> 0
                    "night" -> 1
                    "emergency" -> 2
                    else -> 0
                }
            ) {
                Tab(
                    selected = selectedTab == "peak",
                    onClick = { selectedTab = "peak" },
                    text = { Text("Cao điểm") },
                    icon = { Icon(Icons.Default.Traffic, contentDescription = null) }
                )

                Tab(
                    selected = selectedTab == "night",
                    onClick = { selectedTab = "night" },
                    text = { Text("Đêm") },
                    icon = { Icon(Icons.Default.NightlightRound, contentDescription = null) }
                )

                Tab(
                    selected = selectedTab == "emergency",
                    onClick = { selectedTab = "emergency" },
                    text = { Text("Khẩn cấp") },
                    icon = { Icon(Icons.Default.Warning, contentDescription = null) }
                )
            }

            // Tab content
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                currentStatus?.let { status ->
                    when (selectedTab) {
                        "peak" -> PeakModeTab(
                            currentMode = status.mode,
                            onActivate = { viewModel.activatePeakMode(username) },
                            onDeactivate = { viewModel.activateScheduledMode(username) }
                        )

                        "night" -> NightModeTab(
                            currentMode = status.mode,
                            onActivate = { viewModel.activateNightMode(username) },
                            onDeactivate = { viewModel.activateScheduledMode(username) }
                        )

                        "emergency" -> EmergencyModeTab(
                            currentMode = status.mode,
                            emergencyStatus = emergencyStatus,
                            onActivateA = { viewModel.activateEmergency("A", username) },
                            onActivateB = { viewModel.activateEmergency("B", username) },
                            onDeactivate = { viewModel.deactivateEmergency(username) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PeakModeTab(
    currentMode: String,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit
) {
    val isPeakActive = currentMode.lowercase() == "peak"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon lớn
        Icon(
            Icons.Default.Traffic,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(16.dp),
            tint = Orange
        )

        Text(
            "Chế độ giờ cao điểm",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Ưu tiên kéo dài pha xanh cho hướng có lưu lượng lớn, " +
                    "giảm tần suất dừng xe. Sử dụng khi lưu lượng giao thông tăng cao.",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isPeakActive) {
            // Hiển thị trạng thái đang hoạt động
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Orange.copy(alpha = 0.1f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Chế độ cao điểm đang hoạt động",
                        style = MaterialTheme.typography.titleMedium,
                        color = Orange
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Slider để điều chỉnh thời gian pha
                    Text("Thời gian xanh cho hướng A:")
                    var sliderValueA by remember { mutableFloatStateOf(20f) }
                    Slider(
                        value = sliderValueA,
                        onValueChange = { sliderValueA = it },
                        valueRange = 10f..40f,
                        steps = 6,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Text("${sliderValueA.toInt()} giây")

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Thời gian xanh cho hướng B:")
                    var sliderValueB by remember { mutableFloatStateOf(15f) }
                    Slider(
                        value = sliderValueB,
                        onValueChange = { sliderValueB = it },
                        valueRange = 10f..40f,
                        steps = 6,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Text("${sliderValueB.toInt()} giây")

                    Spacer(modifier = Modifier.height(24.dp))

                    // Nút kết thúc
                    ActionButton(
                        text = "Kết thúc chế độ cao điểm",
                        onClick = onDeactivate,
                        backgroundColor = Color.Gray,
                        icon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    )
                }
            }
        } else {
            // Hiển thị nút kích hoạt
            ActionButton(
                text = "Kích hoạt chế độ cao điểm",
                onClick = onActivate,
                backgroundColor = Orange,
                icon = {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            )
        }
    }
}

@Composable
fun NightModeTab(
    currentMode: String,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit
) {
    val isNightActive = currentMode.lowercase() == "night"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon lớn
        Icon(
            Icons.Default.NightlightRound,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(16.dp),
            tint = Color.DarkGray
        )

        Text(
            "Chế độ đèn nháy vàng ban đêm",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Tất cả đèn vàng nhấp nháy với chu kỳ 1s (ON/OFF). " +
                    "Mục đích cảnh báo, không điều tiết giao thông. " +
                    "Thường hoạt động từ 22:00 đến 05:00.",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isNightActive) {
            // Hiển thị trạng thái đang hoạt động
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.1f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Chế độ đêm đang hoạt động",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Hiển thị đèn nháy
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Hướng A")
                            Spacer(modifier = Modifier.height(8.dp))
                            FlashingYellowLight()
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Hướng B")
                            Spacer(modifier = Modifier.height(8.dp))
                            FlashingYellowLight()
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Nút kết thúc
                    ActionButton(
                        text = "Kết thúc chế độ đêm",
                        onClick = onDeactivate,
                        backgroundColor = Color.Gray,
                        icon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    )
                }
            }
        } else {
            // Hiển thị nút kích hoạt
            ActionButton(
                text = "Kích hoạt chế độ đêm",
                onClick = onActivate,
                backgroundColor = Color.DarkGray,
                icon = {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            )
        }
    }
}

@Composable
fun EmergencyModeTab(
    currentMode: String,
    emergencyStatus: EmergencyStatus?,
    onActivateA: () -> Unit,
    onActivateB: () -> Unit,
    onDeactivate: () -> Unit
) {
    val isEmergencyActive = currentMode.lowercase() == "emergency"
    val priorityDirection = emergencyStatus?.priority_direction ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon lớn
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(16.dp),
            tint = Red
        )

        Text(
            "Chế độ khẩn cấp thủ công",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Ưu tiên hoàn toàn cho một hướng giao thông cụ thể. " +
                    "Hướng được chọn sẽ được đèn xanh liên tục, " +
                    "hướng còn lại sẽ hiển thị đèn đỏ.",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isEmergencyActive) {
            // Hiển thị trạng thái đang hoạt động
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Red.copy(alpha = 0.1f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "CHẾ ĐỘ KHẨN CẤP ĐANG HOẠT ĐỘNG",
                        style = MaterialTheme.typography.titleMedium,
                        color = Red
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Ưu tiên cho hướng $priorityDirection",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Hướng ${if (priorityDirection == "A") "B" else "A"} đang dừng hoàn toàn",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Hiển thị trạng thái đèn
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Đèn hướng A
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Hướng A")
                            TrafficLight(
                                color = if (priorityDirection == "A") Green else Red,
                                size = 40.dp
                            )
                        }

                        // Đèn hướng B
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Hướng B")
                            TrafficLight(
                                color = if (priorityDirection == "B") Green else Red,
                                size = 40.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Nút kết thúc
                    Button(
                        onClick = onDeactivate,
                        colors = ButtonDefaults.buttonColors(containerColor = Red),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "KẾT THÚC CHẾ ĐỘ KHẨN CẤP",
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            // Hiển thị nút kích hoạt
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Chọn hướng ưu tiên:",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Nút ưu tiên hướng A
                Button(
                    onClick = onActivateA,
                    colors = ButtonDefaults.buttonColors(containerColor = Green),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Ưu tiên hướng A",
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nút ưu tiên hướng B
                Button(
                    onClick = onActivateB,
                    colors = ButtonDefaults.buttonColors(containerColor = Green),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Ưu tiên hướng B",
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Red.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        "CHÚ Ý: Khi bật chế độ khẩn cấp, hướng còn lại sẽ hiển thị đèn đỏ liên tục!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}