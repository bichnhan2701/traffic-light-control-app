package com.example.trafficlightcontrol.ui.navigation

import androidx.navigation.NavController

/**
 * Lớp chứa các hành động điều hướng trong ứng dụng
 */
class NavigationActions(private val navController: NavController) {

    // Điều hướng đến Dashboard
    fun navigateToDashboard() {
        navController.navigate(Screen.Dashboard.route) {
            // Pop đến root để tránh nhiều màn hình trùng lặp
            popUpTo(Screen.Dashboard.route) {
                inclusive = true
            }
        }
    }

    // Điều hướng đến Schedule
    fun navigateToSchedule() {
        navController.navigate(Screen.Schedule.route)
    }

    // Điều hướng đến Control với mode
    fun navigateToControl(mode: String) {
        navController.navigate(Screen.DirectControl.createRoute(mode))
    }

    // Điều hướng đến History
    fun navigateToHistory() {
        navController.navigate(Screen.History.route)
    }

    // Điều hướng đến Settings
    fun navigateToSettings() {
        navController.navigate(Screen.Settings.route)
    }

    // Quay lại màn hình trước đó
    fun navigateBack() {
        navController.popBackStack()
    }

    // Điều hướng lên một cấp (từ Control về Dashboard)
    fun navigateUp() {
        navController.navigateUp()
    }
}