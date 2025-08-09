package com.example.trafficlightcontrol.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.trafficlightcontrol.navigation.Screen

enum class AdminNavigationItem(val route: String, val title: String, val icon: @Composable () -> Unit) {
    Dashboard(Screen.AdminDashboard.route, "Dashboard", { Icon(Icons.Default.Dashboard, "Dashboard") }),
    Configuration(Screen.Configuration.route, "Config", { Icon(Icons.Default.Settings, "Configuration") }),
    Users(Screen.UserManagement.route, "Users", { Icon(Icons.Default.People, "Users") }),
    Logs(Screen.Logs.route, "Logs", { Icon(Icons.Default.History, "Logs") })
}

@Composable
fun AdminBottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    BottomAppBar {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            AdminNavigationItem.entries.forEach { item ->
                val selected = currentRoute == item.route
                val tint =
                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

                IconButton(
                    onClick = { onNavigate(item.route) }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = when (item) {
                                AdminNavigationItem.Dashboard -> Icons.Default.Dashboard
                                AdminNavigationItem.Configuration -> Icons.Default.Settings
                                AdminNavigationItem.Users -> Icons.Default.People
                                AdminNavigationItem.Logs -> Icons.Default.History
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