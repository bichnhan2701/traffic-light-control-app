package com.example.trafficlightcontrol.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.trafficlightcontrol.data.model.Mode
import com.example.trafficlightcontrol.ui.component.ActionButton
import com.example.trafficlightcontrol.ui.component.ConfirmPopup
import com.example.trafficlightcontrol.ui.component.FlashingYellowLight
import com.example.trafficlightcontrol.ui.component.LabelActive
import com.example.trafficlightcontrol.ui.component.LabelInactive
import com.example.trafficlightcontrol.ui.component.NumberSlider
import com.example.trafficlightcontrol.ui.component.TrafficLight
import com.example.trafficlightcontrol.ui.theme.*
import com.example.trafficlightcontrol.ui.viewmodel.ControlViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlScreen(
    viewModel: ControlViewModel,
    initialTab: String = "peak",
    navController: NavController
) {
    val isSending by viewModel.isSending.collectAsState()
    val err by viewModel.error.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val priority by viewModel.currentEmergencyPriority.collectAsState()
    val peakA by viewModel.currentPeakA.collectAsState()
    val peakB by viewModel.currentPeakB.collectAsState()

    val scaffoldState = remember { SnackbarHostState() }

    // lỗi một phát —> báo snackbar
    LaunchedEffect(err) {
        err?.let {
            scaffoldState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    // message (đã gửi lệnh, vv.)
    LaunchedEffect(Unit) {
        viewModel.message.collect { scaffoldState.showSnackbar(it) }
    }

    // Tab hiện tại (saveable để xoay màn hình không mất)
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = scaffoldState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                title = {
                    Text(
                        "Điều khiển chi tiết",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs (disable khi đang gửi để tránh spam)
            TabRow(
                selectedTabIndex = when (selectedTab) {
                    "peak" -> 0
                    "night" -> 1
                    "emergency" -> 2
                    else -> 0
                }
            ) {
                Tab(
                    selected = selectedTab == "peak",
                    enabled = !isSending,
                    onClick = { selectedTab = "peak" },
                    text = { Text("Cao điểm") },
                    icon = { Icon(Icons.Default.Traffic, contentDescription = null) },
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    selectedContentColor = MaterialTheme.colorScheme.primary
                )
                Tab(
                    selected = selectedTab == "night",
                    enabled = !isSending,
                    onClick = { selectedTab = "night" },
                    text = { Text("Đêm") },
                    icon = { Icon(Icons.Default.NightsStay, contentDescription = null) },
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    selectedContentColor = MaterialTheme.colorScheme.primary
                )
                Tab(
                    selected = selectedTab == "emergency",
                    enabled = !isSending,
                    onClick = { selectedTab = "emergency" },
                    text = { Text("Khẩn cấp") },
                    icon = { Icon(Icons.Default.Warning, contentDescription = null) },
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    selectedContentColor = MaterialTheme.colorScheme.primary
                )
            }

            // Overlay spinner khi đang gửi lệnh
            Box(Modifier.fillMaxSize()) {
                when (selectedTab) {
                    "peak" -> PeakModeTab(
                        currentMode = currentMode,        // ⬅️ truyền ENUM Mode
                        currentGreenTimeA = peakA,
                        currentGreenTimeB = peakB,
                        onApplyPeak = { a, b -> viewModel.setPeak(a, b) },
                        onDeactivate = { viewModel.setDefault() },
                        enabled = !isSending              // disable controls khi đang gửi
                    )

                    "night" -> NightModeTab(
                        currentMode = currentMode,        // ⬅️ truyền ENUM Mode
                        onActivate = { viewModel.setNight() },
                        onDeactivate = { viewModel.setDefault() },
                        enabled = !isSending
                    )

                    "emergency" -> EmergencyModeTab(
                        isEmergencyActive = (currentMode == Mode.emergency_A || currentMode == Mode.emergency_B),
                        priorityDirection = priority,
                        onActivateA = { viewModel.setEmergencyA() },
                        onActivateB = { viewModel.setEmergencyB() },
                        onDeactivate = { viewModel.setDefault() },
                        enabled = !isSending
                    )
                }

                if (isSending) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = .4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

/* ---------------- Tabs nhận ENUM Mode + enabled ---------------- */

@Composable
fun PeakModeTab(
    currentMode: Mode,
    currentGreenTimeA: Int?,
    currentGreenTimeB: Int?,
    yellowTimeA: Int = 3,
    yellowTimeB: Int = 3,
    onApplyPeak: (Int, Int) -> Unit,
    onDeactivate: () -> Unit,
    enabled: Boolean = true
) {
    val isPeakActive = currentMode == Mode.peak
    var showConfigDialog by remember { mutableStateOf(false) }

    var tempGreenA by remember { mutableFloatStateOf(currentGreenTimeA?.toFloat() ?: 20f) }
    var tempGreenB by remember { mutableFloatStateOf(currentGreenTimeB?.toFloat() ?: 15f) }

    val redTimeA = (currentGreenTimeB ?: 15) + yellowTimeB
    val redTimeB = (currentGreenTimeA ?: 20) + yellowTimeA

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Icon(
                Icons.Default.Traffic, contentDescription = null,
                modifier = Modifier.size(80.dp).padding(16.dp),
                tint = Orange
            )
            Text("Chế độ giờ cao điểm", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            if (isPeakActive) LabelActive() else LabelInactive()
            Spacer(Modifier.height(8.dp))
            Text("Ưu tiên kéo dài pha xanh cho hướng có lưu lượng lớn...", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(20.dp))
        }

        if (isPeakActive) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Orange.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
//                        Text("Trạng thái: ĐANG HOẠT ĐỘNG", style = MaterialTheme.typography.titleMedium, color = Orange)
//                        Spacer(Modifier.height(8.dp))
                        Text("Thông tin pha đèn hiện tại:", style = MaterialTheme.typography.bodyMedium)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Hướng A", fontWeight = FontWeight.Bold)
                            Column {
                                Text("Xanh: ${currentGreenTimeA ?: "--"}s", color = Green)
                                Text("Vàng: $yellowTimeA s", color = Red)
                                Text("Đỏ: $redTimeA s", color = Yellow)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Hướng B", fontWeight = FontWeight.Bold)
                            Column {
                                Text("Xanh: ${currentGreenTimeB ?: "--"}s", color = Green)
                                Text("Vàng: $yellowTimeB s", color = Yellow)
                                Text("Đỏ: $redTimeB s", color = Red)
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = onDeactivate,
                            enabled = enabled,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Kết thúc chế độ cao điểm", color = Color.White)
                        }
                    }
                }
            }
        } else {
            item {
                Button(
                    onClick = {
                        tempGreenA = currentGreenTimeA?.toFloat() ?: 20f
                        tempGreenB = currentGreenTimeB?.toFloat() ?: 15f
                        showConfigDialog = true
                    },
                    enabled = enabled,
                    colors = ButtonDefaults.buttonColors(containerColor = Orange),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Cấu hình & kích hoạt cao điểm", color = Color.White)
                }
            }
        }
    }

    if (showConfigDialog) {
        AlertDialog(
            onDismissRequest = { showConfigDialog = false },
            title = { Text("Cấu hình thời gian cao điểm") },
            text = {
                Column {
                    Text("Điều chỉnh thời gian xanh cho từng hướng", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Text("Hướng A", fontWeight = FontWeight.Bold)
                    NumberSlider(
                        value = tempGreenA,
                        onValueChange = { tempGreenA = it },
                        valueRange = 10f..150f,
                        step = 1
                    )
                    Text("${tempGreenA.toInt()} giây", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Hướng B", fontWeight = FontWeight.Bold)
                    NumberSlider(
                        value = tempGreenB,
                        onValueChange = { tempGreenB = it },
                        valueRange = 10f..150f,
                        step = 1
                    )
                    Text("${tempGreenB.toInt()} giây", style = MaterialTheme.typography.bodySmall)

                    Spacer(Modifier.height(16.dp))
                    val futureRedA = tempGreenB.toInt() + yellowTimeB
                    val futureRedB = tempGreenA.toInt() + yellowTimeA
                    Divider(Modifier.padding(vertical = 8.dp))
                    Text("Các pha đèn sẽ áp dụng:", style = MaterialTheme.typography.bodyMedium)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Hướng A")
                        Text("Xanh: ${tempGreenA.toInt()}s, Vàng: $yellowTimeA s, Đỏ: $futureRedA s")
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Hướng B")
                        Text("Xanh: ${tempGreenB.toInt()}s, Vàng: $yellowTimeB s, Đỏ: $futureRedB s")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onApplyPeak(tempGreenA.toInt(), tempGreenB.toInt())
                        showConfigDialog = false
                    },
                    enabled = enabled,
                    colors = ButtonDefaults.buttonColors(containerColor = Orange)
                ) { Text("Áp dụng", color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfigDialog = false }) { Text("Hủy") }
            }
        )
    }
}

@Composable
fun NightModeTab(
    currentMode: Mode,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit,
    enabled: Boolean = true
) {
    val isNightActive = currentMode == Mode.night
    var showConfirmPopup by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Icon(
                Icons.Default.NightsStay, contentDescription = null,
                modifier = Modifier.size(80.dp).padding(16.dp),
                tint = Color.DarkGray
            )
            Text("Chế độ đèn nháy vàng ban đêm", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            if (isNightActive) LabelActive() else LabelInactive()
            Spacer(Modifier.height(8.dp))
            Text(
                "Tất cả đèn vàng nhấp nháy",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(20.dp))
        }

        if (isNightActive) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
//                        Text("Trạng thái: ĐANG HOẠT ĐỘNG", style = MaterialTheme.typography.titleMedium, color = Color.DarkGray)
//                        Spacer(Modifier.height(24.dp))
                        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Hướng A"); FlashingYellowLight() }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Hướng B"); FlashingYellowLight() }
                        }
                        Spacer(Modifier.height(32.dp))
                        ActionButton(
                            text = "Kết thúc chế độ đêm",
                            onClick = onDeactivate,
                            backgroundColor = Color.Gray,
                            icon = { Icon(Icons.Default.Close, contentDescription = null, tint = Color.White) }
                        )
                    }
                }
            }
        } else {
            item {
                Button(
                    onClick = { showConfirmPopup = true },
                    enabled = enabled,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Kích hoạt chế độ đêm", color = Color.White)
                }
            }
        }
    }

    if (showConfirmPopup) {
        ConfirmPopup(
            title = "Xác nhận chuyển sang chế độ đêm",
            message = "Bạn có chắc chắn muốn chuyển sang chế độ đèn nháy vàng ban đêm?",
            onConfirm = { onActivate(); showConfirmPopup = false },
            onDismiss = { showConfirmPopup = false }
        )
    }
}

@Composable
fun EmergencyModeTab(
    isEmergencyActive: Boolean,
    priorityDirection: String?,
    onActivateA: () -> Unit,
    onActivateB: () -> Unit,
    onDeactivate: () -> Unit,
    enabled: Boolean = true
) {
    var showConfirmPopupA by remember { mutableStateOf(false) }
    var showConfirmPopupB by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Icon(
                Icons.Default.Warning, contentDescription = null,
                modifier = Modifier.size(80.dp).padding(16.dp),
                tint = Red
            )
            Text("Chế độ khẩn cấp thủ công", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            if (isEmergencyActive) LabelActive() else LabelInactive()
            Spacer(Modifier.height(8.dp))
            Text("Ưu tiên hoàn toàn cho một hướng", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(20.dp))
        }

        if (isEmergencyActive) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Red.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
//                        Text("CHẾ ĐỘ KHẨN CẤP ĐANG HOẠT ĐỘNG", style = MaterialTheme.typography.titleMedium, color = Red)
//                        Spacer(Modifier.height(16.dp))
                        Text("Ưu tiên cho hướng ${priorityDirection ?: "-"}", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        Text("Hướng ${if (priorityDirection == "A") "B" else "A"} đang dừng hoàn toàn", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(24.dp))
                        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Hướng A")
                                TrafficLight(color = if (priorityDirection == "A") Green else Red, size = 40.dp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Hướng B")
                                TrafficLight(color = if (priorityDirection == "B") Green else Red, size = 40.dp)
                            }
                        }
                        Spacer(Modifier.height(32.dp))
                        Button(
                            onClick = onDeactivate,
                            enabled = enabled,
                            colors = ButtonDefaults.buttonColors(containerColor = Red),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Kết thúc chế độ", color = Color.White)
                        }
                    }
                }
            }
        } else {
            item {
                Column(Modifier.fillMaxWidth()) {
                    Text("Chọn hướng ưu tiên:", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { showConfirmPopupA = true },
                        enabled = enabled,
                        colors = ButtonDefaults.buttonColors(containerColor = Green),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Ưu tiên hướng A", color = Color.White)
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { showConfirmPopupB = true },
                        enabled = enabled,
                        colors = ButtonDefaults.buttonColors(containerColor = Green),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Ưu tiên hướng B", color = Color.White)
                    }
                    Spacer(Modifier.height(24.dp))
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

    if (showConfirmPopupA) {
        ConfirmPopup(
            title = "Xác nhận chuyển sang chế độ khẩn cấp",
            message = "Bạn có chắc muốn ưu tiên hướng A? Hướng B sẽ luôn đỏ.",
            onConfirm = { onActivateA(); showConfirmPopupA = false },
            onDismiss = { showConfirmPopupA = false }
        )
    }
    if (showConfirmPopupB) {
        ConfirmPopup(
            title = "Xác nhận chuyển sang chế độ khẩn cấp",
            message = "Bạn có chắc muốn ưu tiên hướng B? Hướng A sẽ luôn đỏ.",
            onConfirm = { onActivateB(); showConfirmPopupB = false },
            onDismiss = { showConfirmPopupB = false }
        )
    }
}
