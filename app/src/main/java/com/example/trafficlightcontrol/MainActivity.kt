package com.example.trafficlightcontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.trafficlightcontrol.data.repository.TrafficLightRepository
import com.example.trafficlightcontrol.ui.component.TrafficLightBottomNavigation
import com.example.trafficlightcontrol.ui.navigation.TrafficLightNavGraph
import com.example.trafficlightcontrol.ui.theme.TrafficLightControlTheme
import com.example.trafficlightcontrol.ui.viewmodel.*
import com.google.firebase.database.FirebaseDatabase

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
    // Firebase đã init trong Application
    val db = remember { FirebaseDatabase.getInstance() }
    val appScope = rememberCoroutineScope()
    val repo = remember(db, appScope) {
        TrafficLightRepository(db = db, appScope = appScope)
    }

    // Ngã tư đang điều khiển
    val intersectionId = remember { "cs-qt-001" }

    // DashboardViewModel
    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(
                    db = db,
                    repo = repo,
                    intersectionId = intersectionId
                ) as T
            }
        }
    )

    // ControlViewModel
    val controlViewModel: ControlViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ControlViewModel(repo = repo, intersectionId = intersectionId
                ) as T
            }
        }
    )

    // HistoryViewModel
    val historyViewModel: HistoryViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HistoryViewModel(repo = repo, intersectionId = intersectionId) as T
            }
        }
    )

    // Navigation
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            TrafficLightBottomNavigation(navController = navController)
        }
    ) { innerPadding ->
        TrafficLightNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            dashboardViewModel = dashboardViewModel,
            controlViewModel = controlViewModel,
            historyViewModel = historyViewModel
        )
    }
}
