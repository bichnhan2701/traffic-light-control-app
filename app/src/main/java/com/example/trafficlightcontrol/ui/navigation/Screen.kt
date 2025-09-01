package com.example.trafficlightcontrol.ui.navigation

/**
 * Định nghĩa các tuyến đường có trong ứng dụng
 */
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Control : Screen("control/{mode}")
    object DirectControl : Screen("control/{mode}")  // Used with arguments
    object History : Screen("history")
    object Settings : Screen("settings")

    // Hàm tiện ích để tạo route với tham số
    fun createRoute(mode: String): String {
        return "control/$mode"
    }
}
