package com.example.trafficlightcontrol.ui.component

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import com.example.trafficlightcontrol.domain.model.UserRole
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Logout

@Composable
fun BottomNavigationBar(
    selectedScreen: String,
    onScreenSelected: (String) -> Unit,
    userRole: UserRole
) {
    NavigationBar {
        if (userRole == UserRole.ADMIN) {
            NavigationBarItem(
                selected = selectedScreen == "admin_dashboard",
                onClick = { onScreenSelected("admin_dashboard") },
                icon = { Icon(Icons.Filled.Home, contentDescription = "Dashboard") },
                label = { Text("Dashboard") }
            )
            NavigationBarItem(
                selected = selectedScreen == "configuration",
                onClick = { onScreenSelected("configuration") },
                icon = { Icon(Icons.Filled.Settings, contentDescription = "Configuration") },
                label = { Text("Config") }
            )
            NavigationBarItem(
                selected = selectedScreen == "logs",
                onClick = { onScreenSelected("logs") },
                icon = { Icon(Icons.Filled.List, contentDescription = "Logs") },
                label = { Text("Logs") }
            )
        } else {
            NavigationBarItem(
                selected = selectedScreen == "operator_traffic_status",
                onClick = { onScreenSelected("operator_traffic_status") },
                icon = { Icon(Icons.Filled.Home, contentDescription = "Traffic Status") },
                label = { Text("Traffic") }
            )
            NavigationBarItem(
                selected = selectedScreen == "operator_history",
                onClick = { onScreenSelected("operator_history") },
                icon = { Icon(Icons.Filled.List, contentDescription = "History") },
                label = { Text("History") }
            )
        }
        NavigationBarItem(
            selected = false,
            onClick = { onScreenSelected("logout") },
            icon = { Icon(Icons.Filled.Logout, contentDescription = "Logout") },
            label = { Text("Logout") }
        )
    }
}