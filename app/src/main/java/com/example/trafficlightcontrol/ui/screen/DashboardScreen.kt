package com.example.trafficlightcontrol.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trafficlightcontrol.ui.component.ModeCard
import com.example.trafficlightcontrol.ui.component.TrafficLightDisplay
import com.example.trafficlightcontrol.ui.viewmodel.DashboardViewModel
import androidx.compose.foundation.lazy.LazyColumn
import com.example.trafficlightcontrol.data.model.Durations
import com.example.trafficlightcontrol.data.model.Mode
import com.example.trafficlightcontrol.data.model.Phase
import com.example.trafficlightcontrol.data.model.UiLights
import com.example.trafficlightcontrol.data.repository.toUiLights
import com.example.trafficlightcontrol.ui.component.ConnectionStatusBadge
import com.example.trafficlightcontrol.ui.component.ModeStatusBadge
import com.example.trafficlightcontrol.ui.component.PhaseConfigCard
import com.example.trafficlightcontrol.ui.theme.Orange
import com.example.trafficlightcontrol.ui.theme.Red

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    navigateToControl: (String) -> Unit
) {
    val ui by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val isLoading = ui.isLoading
    val error = ui.error
    val p = ui.phasePanel

    val lastSeen = ui.esp?.lastSeenAt ?: 0L
    val now = ui.serverNow
    val agoMs = (now - lastSeen).coerceAtLeast(0L)

    // ép kiểu rõ ràng để tránh suy luận thành Any
    val lightsForUi: UiLights = ui.lights?.toUiLights() ?: UiLights()

    LaunchedEffect(error) {
        if (!error.isNullOrEmpty()) {
            snackbarHostState.showSnackbar(error, withDismissAction = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
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
                    ConnectionStatusBadge(
                        label = "ESP",
                        online = ui.esp?.online,
                        agoMs = agoMs,
                        windowMs = 20_000L
                    )
                    Spacer(Modifier.width(16.dp))
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Khối mô phỏng đèn + tiến độ pha ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp).padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment =  Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Trạng thái hiện tại",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        ModeStatusBadge(mode = p?.mode?.name ?: Mode.default.name)
                    }
                    Spacer(Modifier.height(8.dp))
                    TrafficLightDisplay(
                        mode = p?.mode ?: Mode.default,
                        phase = p?.phase ?: Phase.A_GREEN,
                        timeLeftMs = p?.timeLeftMs ?: 0L,
                        durations = ui.durations ?: Durations(),
                        lights = lightsForUi
                    )
                }

                item{
                    Spacer(Modifier.height(8.dp))
                    // Header + chip mode
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Thông tin pha hiện tại",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    PhaseConfigCard(
                        mode = p?.mode ?: Mode.default,
                        durations = ui.durations ?: Durations(),
                    )
                }

                // --- Khối điều khiển nhanh ---
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Chế độ điều khiển", style = MaterialTheme.typography.titleMedium)
                    ModeCard(
                        title = "Chế độ giờ cao điểm",
                        color = Orange,
                        icon = { Icon(Icons.Default.Traffic, contentDescription = null, tint = Orange) },
                        onClick = { navigateToControl("peak") }
                    )
                    ModeCard(
                        title = "Chế độ đêm",
                        color = Color.DarkGray,
                        icon = { Icon(Icons.Default.NightsStay, contentDescription = null, tint = Color.DarkGray) },
                        onClick = { navigateToControl("night") }
                    )
                    ModeCard(
                        title = "Chế độ ưu tiên khẩn cấp",
                        color = Red,
                        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Red) },
                        onClick = { navigateToControl("emergency") }
                    )
                }
            }
        }
    }
}
