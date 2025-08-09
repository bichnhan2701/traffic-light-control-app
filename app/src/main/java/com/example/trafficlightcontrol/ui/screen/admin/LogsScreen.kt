package com.example.trafficlightcontrol.ui.screen.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.trafficlightcontrol.domain.model.ActionLog
import com.example.trafficlightcontrol.domain.model.ActionType
import com.example.trafficlightcontrol.navigation.Screen
import com.example.trafficlightcontrol.ui.component.AdminBottomNavigationBar
import com.example.trafficlightcontrol.ui.component.LoadingIndicator
import com.example.trafficlightcontrol.ui.viewmodel.admin.LogsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToConfiguration: () -> Unit,
    onNavigateToUserManagement: () -> Unit,
    viewModel: LogsViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var showFilterDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Logs") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToDashboard) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    IconButton(onClick = { viewModel.exportLogs() }) {
                        Icon(Icons.Default.Share, contentDescription = "Export")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                AdminBottomNavigationBar(
                    currentRoute = Screen.Logs.route,
                    onNavigate = { route ->
                        when (route) {
                            Screen.AdminDashboard.route -> onNavigateToDashboard()
                            Screen.UserManagement.route -> onNavigateToUserManagement()
                            Screen.Configuration.route -> onNavigateToConfiguration()
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
                // Filter chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedActionType == null,
                            onClick = { viewModel.filterByActionType(null) },
                            label = { Text("All") }
                        )
                    }

                    ActionType.values().forEach { actionType ->
                        item {
                            FilterChip(
                                selected = state.selectedActionType == actionType,
                                onClick = { viewModel.filterByActionType(actionType) },
                                label = { Text(actionType.name.replace("_", " ")) }
                            )
                        }
                    }
                }

                if (state.dateRange != null) {
                    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Date Range: ${state.dateRange.first.format(formatter)} - ${state.dateRange.second.format(formatter)}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearDateFilter() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear filter")
                        }
                    }
                }

                if (state.logs.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "No logs found.\nTry adjusting your filters.",
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(state.logs) { log ->
                            LogItem(log = log)
                        }
                    }
                }

                if (state.error != null) {
                    Snackbar(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(state.error)
                    }
                }
            }
        }
    }

    // Filter dialog
    if (showFilterDialog) {
        DateFilterDialog(
            onDismiss = { showFilterDialog = false },
            onApplyFilter = { startDate, endDate ->
                viewModel.filterByDate(startDate, endDate)
                showFilterDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterDialog(
    onDismiss: () -> Unit,
    onApplyFilter: (LocalDate, LocalDate) -> Unit
) {
    val today = LocalDate.now()
    var startDate by remember { mutableStateOf(today.minusDays(7)) }
    var endDate by remember { mutableStateOf(today) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter by Date") },
        text = {
            Column {
                Text(
                    text = "Start Date",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                // In a real app, you would use a DatePicker here
                // For simplicity, we'll use a simple TextField
                Text(
                    text = startDate.toString(),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = { startDate = startDate.minusDays(1) },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text("Previous Day")
                }

                Button(
                    onClick = { startDate = startDate.plusDays(1) },
                    modifier = Modifier.padding(bottom = 16.dp),
                    enabled = startDate.isBefore(endDate)
                ) {
                    Text("Next Day")
                }

                Text(
                    text = "End Date",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Text(
                    text = endDate.toString(),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = { endDate = endDate.minusDays(1) },
                    modifier = Modifier.padding(bottom = 8.dp),
                    enabled = endDate.isAfter(startDate)
                ) {
                    Text("Previous Day")
                }

                Button(
                    onClick = { endDate = endDate.plusDays(1) },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Next Day")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApplyFilter(startDate, endDate) }
            ) {
                Text("APPLY")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
fun LogItem(log: ActionLog) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = log.userName,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = log.timestamp.format(formatter),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                val actionColor = when (log.actionType) {
                    ActionType.MANUAL_OVERRIDE -> MaterialTheme.colorScheme.error
                    ActionType.PHASE_CHANGE -> MaterialTheme.colorScheme.tertiary
                    ActionType.CONFIG_UPDATE -> MaterialTheme.colorScheme.primary
                    ActionType.USER_MANAGEMENT -> MaterialTheme.colorScheme.secondary
                    ActionType.LOGIN, ActionType.LOGOUT -> MaterialTheme.colorScheme.tertiary
                }

                Surface(
                    color = actionColor,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = log.actionType.name.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = log.userRole.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = log.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (log.previousState.isNotEmpty()) {
                Text(
                    text = "Previous: ${log.previousState}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (log.newState.isNotEmpty()) {
                Text(
                    text = "New: ${log.newState}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}