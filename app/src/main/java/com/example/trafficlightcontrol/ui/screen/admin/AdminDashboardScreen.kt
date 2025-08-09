package com.example.trafficlightcontrol.ui.screen.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trafficlightcontrol.domain.model.Direction
import com.example.trafficlightcontrol.domain.model.LightPhase
import com.example.trafficlightcontrol.navigation.Screen
import com.example.trafficlightcontrol.ui.component.AdminBottomNavigationBar
import com.example.trafficlightcontrol.ui.component.LoadingIndicator
import com.example.trafficlightcontrol.ui.component.TrafficLightDisplay
import com.example.trafficlightcontrol.ui.viewmodel.admin.AdminDashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateToConfiguration: () -> Unit,
    onNavigateToUserManagement: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToBluetooth: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var showManualOverrideDialog by remember { mutableStateOf(false) }
    var selectedDirection by remember { mutableStateOf(Direction.NORTH_SOUTH) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(onClick = onNavigateToBluetooth) {
                        Icon(Icons.Default.Bluetooth, contentDescription = "Bluetooth Connection")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                AdminBottomNavigationBar(
                    currentRoute = Screen.AdminDashboard.route,
                    onNavigate = { route ->
                        when (route) {
                            Screen.Configuration.route -> onNavigateToConfiguration()
                            Screen.UserManagement.route -> onNavigateToUserManagement()
                            Screen.Logs.route -> onNavigateToLogs()
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            LoadingIndicator(fullScreen = true)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Text(
                    text = "Traffic Lights Status",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )

                // Active configuration info
                state.activeConfiguration?.let { config ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Active Configuration: ${config.name}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Red: ${config.redDuration}s")
                                Text("Yellow: ${config.yellowDuration}s")
                                Text("Green: ${config.greenDuration}s")
                            }
                        }
                    }
                }

                // Traffic Light Displays
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // North-South Traffic Light
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
                                Text("Manual Override")
                            }
                        }
                    }

                    // East-West Traffic Light
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
                                Text("Manual Override")
                            }
                        }
                    }
                }

                // Recent activity
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Recent Activity",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (state.recentLogs.isEmpty()) {
                            Text("No recent activity")
                        } else {
                            state.recentLogs.take(5).forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = log.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = log.timestamp.toString(),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Manual Override Dialog
    if (showManualOverrideDialog) {
        AlertDialog(
            onDismissRequest = { showManualOverrideDialog = false },
            title = { Text("Manual Override") },
            text = {
                Column {
                    Text("Select phase for ${selectedDirection.name.replace("_", "-")}:")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                viewModel.changePhase(selectedDirection, LightPhase.RED)
                                showManualOverrideDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("RED")
                        }
                        Button(
                            onClick = {
                                viewModel.changePhase(selectedDirection, LightPhase.YELLOW)
                                showManualOverrideDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Text("YELLOW")
                        }
                        Button(
                            onClick = {
                                viewModel.changePhase(selectedDirection, LightPhase.GREEN)
                                showManualOverrideDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("GREEN")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetManualOverride(selectedDirection)
                        showManualOverrideDialog = false
                    }
                ) {
                    Text("RESET AUTO")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualOverrideDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}