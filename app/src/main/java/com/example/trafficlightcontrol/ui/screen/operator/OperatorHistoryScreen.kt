package com.example.trafficlightcontrol.ui.screen.operator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trafficlightcontrol.domain.model.ActionLog
import com.example.trafficlightcontrol.navigation.Screen
import com.example.trafficlightcontrol.ui.component.LoadingIndicator
import com.example.trafficlightcontrol.ui.component.OperatorBottomNavigationBar
import com.example.trafficlightcontrol.ui.viewmodel.operator.OperatorHistoryViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: OperatorHistoryViewModel = hiltViewModel()
) {
    val state = viewModel.state

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Activity History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            OperatorBottomNavigationBar(
                currentRoute = Screen.OperatorHistory.route,
                onNavigate = { route ->
                    if (route == Screen.OperatorTrafficStatus.route) {
                        onNavigateBack()
                    }
                }
            )
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
                if (state.logs.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "No activity history found.",
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(state.logs) { log ->
                            OperatorLogItem(log = log)
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
}

@Composable
fun OperatorLogItem(log: ActionLog) {
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
                    text = log.actionType.name.replace("_", " "),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = log.timestamp.format(formatter),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

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