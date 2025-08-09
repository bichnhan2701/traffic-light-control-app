package com.example.trafficlightcontrol.ui.screen.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trafficlightcontrol.domain.model.PhaseConfiguration
import com.example.trafficlightcontrol.domain.model.TimeRange
import com.example.trafficlightcontrol.navigation.Screen
import com.example.trafficlightcontrol.ui.component.AdminBottomNavigationBar
import com.example.trafficlightcontrol.ui.component.LoadingIndicator
import com.example.trafficlightcontrol.ui.viewmodel.admin.ConfigurationViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToUserManagement: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToBluetooth: () -> Unit,
    viewModel: ConfigurationViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var showAddEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var selectedConfiguration by remember { mutableStateOf<PhaseConfiguration?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phase Configurations") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToDashboard) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToBluetooth) {
                        Icon(Icons.Default.Bluetooth, contentDescription = "Bluetooth Connection")
                    }
                    IconButton(onClick = {
                        selectedConfiguration = null
                        showAddEditDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Configuration")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                AdminBottomNavigationBar(
                    currentRoute = Screen.Configuration.route,
                    onNavigate = { route ->
                        when (route) {
                            Screen.AdminDashboard.route -> onNavigateToDashboard()
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
            ) {
                if (state.configurations.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "No configurations found.\nTap + to add one.",
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(state.configurations) { config ->
                            ConfigurationCard(
                                config = config,
                                onEdit = {
                                    selectedConfiguration = config
                                    showAddEditDialog = true
                                },
                                onDelete = {
                                    selectedConfiguration = config
                                    showDeleteConfirmDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add/Edit Dialog
    if (showAddEditDialog) {
        ConfigurationDialog(
            configuration = selectedConfiguration,
            onDismiss = { showAddEditDialog = false },
            onSave = { config ->
                viewModel.saveConfiguration(config)
                showAddEditDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && selectedConfiguration != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete the configuration '${selectedConfiguration?.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        selectedConfiguration?.let { viewModel.deleteConfiguration(it.id) }
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("DELETE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationCard(
    config: PhaseConfiguration,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = config.name,
                    style = MaterialTheme.typography.titleLarge
                )

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Time: ${config.timeRange.start.format(formatter)} - ${config.timeRange.end.format(formatter)}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PhaseTimeInfo("Red", config.redDuration)
                PhaseTimeInfo("Yellow", config.yellowDuration)
                PhaseTimeInfo("Green", config.greenDuration)
            }
        }
    }
}

@Composable
fun PhaseTimeInfo(phaseName: String, duration: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = phaseName,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "$duration sec",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationDialog(
    configuration: PhaseConfiguration?,
    onDismiss: () -> Unit,
    onSave: (PhaseConfiguration) -> Unit
) {
    val isNewConfig = configuration == null
    val title = if (isNewConfig) "Add Configuration" else "Edit Configuration"

    var name by remember { mutableStateOf(configuration?.name ?: "Morning Rush") }
    var startHour by remember { mutableStateOf(configuration?.timeRange?.start?.hour ?: 7) }
    var startMinute by remember { mutableStateOf(configuration?.timeRange?.start?.minute ?: 0) }
    var endHour by remember { mutableStateOf(configuration?.timeRange?.end?.hour ?: 10) }
    var endMinute by remember { mutableStateOf(configuration?.timeRange?.end?.minute ?: 0) }
    var redDuration by remember { mutableStateOf(configuration?.redDuration?.toString() ?: "60") }
    var yellowDuration by remember { mutableStateOf(configuration?.yellowDuration?.toString() ?: "5") }
    var greenDuration by remember { mutableStateOf(configuration?.greenDuration?.toString() ?: "55") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Configuration Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                Text(
                    text = "Time Range",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Start Time
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Start Time")
                        Row {
                            OutlinedTextField(
                                value = startHour.toString().padStart(2, '0'),
                                onValueChange = { value ->
                                    val hour = value.toIntOrNull()
                                    if (hour != null && hour in 0..23) {
                                        startHour = hour
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                label = { Text("H") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = startMinute.toString().padStart(2, '0'),
                                onValueChange = { value ->
                                    val minute = value.toIntOrNull()
                                    if (minute != null && minute in 0..59) {
                                        startMinute = minute
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                label = { Text("M") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // End Time
                    Column(modifier = Modifier.weight(1f)) {
                        Text("End Time")
                        Row {
                            OutlinedTextField(
                                value = endHour.toString().padStart(2, '0'),
                                onValueChange = { value ->
                                    val hour = value.toIntOrNull()
                                    if (hour != null && hour in 0..23) {
                                        endHour = hour
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                label = { Text("H") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = endMinute.toString().padStart(2, '0'),
                                onValueChange = { value ->
                                    val minute = value.toIntOrNull()
                                    if (minute != null && minute in 0..59) {
                                        endMinute = minute
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                label = { Text("M") }
                            )
                        }
                    }
                }

                Text(
                    text = "Phase Durations (seconds)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = redDuration,
                        onValueChange = { value ->
                            val duration = value.toIntOrNull()
                            if (duration != null && duration > 0) {
                                redDuration = value
                            }
                        },
                        label = { Text("Red") },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = yellowDuration,
                        onValueChange = { value ->
                            val duration = value.toIntOrNull()
                            if (duration != null && duration > 0) {
                                yellowDuration = value
                            }
                        },
                        label = { Text("Yellow") },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = greenDuration,
                        onValueChange = { value ->
                            val duration = value.toIntOrNull()
                            if (duration != null && duration > 0) {
                                greenDuration = value
                            }
                        },
                        label = { Text("Green") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val startTime = LocalTime.of(startHour, startMinute)
                    val endTime = LocalTime.of(endHour, endMinute)
                    val timeRange = TimeRange(startTime, endTime)

                    val config = PhaseConfiguration(
                        id = configuration?.id ?: "",
                        name = name,
                        timeRange = timeRange,
                        redDuration = redDuration.toIntOrNull() ?: 60,
                        yellowDuration = yellowDuration.toIntOrNull() ?: 5,
                        greenDuration = greenDuration.toIntOrNull() ?: 55
                    )

                    onSave(config)
                }
            ) {
                Text("SAVE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}