package com.example.trafficlightcontrol.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.trafficlightcontrol.ui.navigation.Screen
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text

/**
 * Model cho các mục trong bottom navigation
 */
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

/**
 * Bottom Navigation cho ứng dụng
 */
@Composable
fun TrafficLightBottomNavigation(
    navController: NavController
) {
    val items = listOf(
        BottomNavItem(
            route = Screen.Dashboard.route,
            label = "Trang chủ",
            icon = Icons.Default.Home
        ),
        BottomNavItem(
            route = Screen.Schedule.route,
            label = "Lịch",
            icon = Icons.Default.CalendarToday
        ),
        BottomNavItem(
            route = Screen.History.route,
            label = "Lịch sử",
            icon = Icons.Default.History
        ),
        BottomNavItem(
            route = Screen.Settings.route,
            label = "Cài đặt",
            icon = Icons.Default.Settings
        )
    )

    NavigationBar (
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                selected = currentDestination == item.route ||
                        // Xử lý trường hợp route có tham số
                        (currentDestination?.startsWith("control/") == true &&
                                item.route == Screen.Dashboard.route),
                onClick = {
                    navController.navigate(item.route) {
                        // Tránh tạo nhiều bản sao của cùng một màn hình
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}