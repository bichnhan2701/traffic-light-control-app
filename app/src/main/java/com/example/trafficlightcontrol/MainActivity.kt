package com.example.trafficlightcontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.trafficlightcontrol.data.repository.TrafficLightRepository
import com.example.trafficlightcontrol.ui.component.TrafficLightBottomNavigation
import com.example.trafficlightcontrol.ui.navigation.NavigationActions
import com.example.trafficlightcontrol.ui.navigation.TrafficLightNavGraph
import com.example.trafficlightcontrol.ui.theme.TrafficLightControlTheme
import com.example.trafficlightcontrol.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrafficLightControlTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    // Repository
    val repository = remember { TrafficLightRepository() }

    // ViewModels
    val dashboardViewModel = viewModel { DashboardViewModel(repository) }
    val scheduleViewModel = viewModel { ScheduleViewModel(repository) }
    val controlViewModel = viewModel { ControlViewModel(repository) }
    val historyViewModel = viewModel { HistoryViewModel(repository) }

    // Username (có thể thay thế bằng hệ thống đăng nhập thực tế)
    val username = "bich-nhan"

    // Navigation
    val navController = rememberNavController()
    val navigationActions = remember(navController) {
        NavigationActions(navController)
    }

    Scaffold(
        bottomBar = {
            TrafficLightBottomNavigation(navController = navController)
        }
    ) { innerPadding ->
        TrafficLightNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            username = username,
            dashboardViewModel = dashboardViewModel,
            scheduleViewModel = scheduleViewModel,
            controlViewModel = controlViewModel,
            historyViewModel = historyViewModel
        )
    }
}