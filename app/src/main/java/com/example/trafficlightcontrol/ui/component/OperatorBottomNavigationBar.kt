package com.example.trafficlightcontrol.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.trafficlightcontrol.navigation.Screen

enum class OperatorNavigationItem(val route: String, val title: String) {
    TrafficStatus(Screen.OperatorTrafficStatus.route, "Status"),
    History(Screen.OperatorHistory.route, "History")
}

@Composable
fun OperatorBottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    BottomAppBar {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OperatorNavigationItem.entries.forEach { item ->
                val selected = currentRoute == item.route
                val tint =
                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

                IconButton(
                    onClick = { onNavigate(item.route) }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = when (item) {
                                OperatorNavigationItem.TrafficStatus -> CustomIcons.TrafficSignal
                                OperatorNavigationItem.History -> Icons.Default.History
                            },
                            contentDescription = item.title,
                            tint = tint
                        )
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = tint
                        )
                    }
                }
            }
        }
    }
}