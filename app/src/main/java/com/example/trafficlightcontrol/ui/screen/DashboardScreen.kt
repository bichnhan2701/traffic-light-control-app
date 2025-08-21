package com.example.trafficlightcontrol.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trafficlightcontrol.ui.component.ConnectionStatusIndicator
import com.example.trafficlightcontrol.ui.component.ModeCard
import com.example.trafficlightcontrol.ui.component.TrafficLightDisplay
import com.example.trafficlightcontrol.ui.theme.*
import com.example.trafficlightcontrol.ui.viewmodel.DashboardViewModel
import androidx.compose.foundation.lazy.LazyColumn

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    navigateToSchedule: () -> Unit,
    navigateToControl: (String) -> Unit
) {
    val currentStatus by viewModel.currentStatus.collectAsState()
    val systemStatus by viewModel.systemStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    // Hiển thị Snackbar khi có lỗi
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                actionLabel = "Đóng"
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {
                    Text(
                        "Traffic Light Control",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    // Hiển thị trạng thái kết nối
                    systemStatus?.let {
                        ConnectionStatusIndicator(isConnected = it.esp32_connected)
                    }

                    // Nút đồng bộ thời gian
                    IconButton(onClick = { viewModel.syncTime() }) {
                        Icon(Icons.Default.Sync, contentDescription = "Đồng bộ thời gian")
                    }
                }
            )
        },
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                currentStatus?.let { status ->
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        TrafficLightDisplay(
                            mode = status.mode,
                            currentPhase = status.phase,
                            timeLeft = status.time_left,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    item {
                        Text(
                            "Điều khiển nhanh",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ModeCard(
                                title = "Mặc định",
                                color = Blue,
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "Theo lịch",
                                        tint = Blue
                                    )
                                },
                                onClick = navigateToSchedule
                            )
                            ModeCard(
                                title = "Cao điểm",
                                color = Orange,
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Traffic,
                                        contentDescription = "Cao điểm",
                                        tint = Orange
                                    )
                                },
                                onClick = { navigateToControl("peak") }
                            )
                        }
                    }
                    item {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ModeCard(
                                title = "Đêm",
                                color = Color.DarkGray,
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.NightlightRound,
                                        contentDescription = "Đêm",
                                        tint = Color.DarkGray
                                    )
                                },
                                onClick = { navigateToControl("night") }
                            )
                            ModeCard(
                                title = "Khẩn cấp",
                                color = Red,
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Khẩn cấp",
                                        tint = Red
                                    )
                                },
                                onClick = { navigateToControl("emergency") }
                            )
                        }
                    }
                }
            }
        }
    }
}
