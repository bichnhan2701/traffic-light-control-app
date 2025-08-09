package com.example.trafficlightcontrol.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Login : Screen("login")
    object AdminDashboard : Screen("admin_dashboard")
    object Configuration : Screen("configuration")
    object UserManagement : Screen("user_management")
    object Logs : Screen("logs")
    object OperatorTrafficStatus : Screen("operator_traffic_status")
    object OperatorHistory : Screen("operator_history")
    object BluetoothDevice : Screen("bluetooth_device")
}

data class NavigationItem(val screen: Screen, val label: String, val icon: Int)

val navigationItems = listOf(
    NavigationItem(Screen.Home, "Home", android.R.drawable.ic_menu_view),
    NavigationItem(Screen.Profile, "Profile", android.R.drawable.ic_menu_myplaces),
    NavigationItem(Screen.BluetoothDevice, "Bluetooth", android.R.drawable.stat_sys_data_bluetooth)
)
