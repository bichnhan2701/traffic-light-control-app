package com.example.trafficlightcontrol.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.trafficlightcontrol.ui.screen.*
import com.example.trafficlightcontrol.ui.viewmodel.*

/**
 * Navigation Graph chính của ứng dụng
 */
@Composable
fun TrafficLightNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    username: String,
    dashboardViewModel: DashboardViewModel,
    scheduleViewModel: ScheduleViewModel,
    controlViewModel: ControlViewModel,
    historyViewModel: HistoryViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
    ) {
        // Màn hình Dashboard
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = dashboardViewModel,
                navigateToSchedule = { navController.navigate(Screen.Schedule.route) },
                navigateToControl = { mode ->
                    navController.navigate(Screen.DirectControl.createRoute(mode))
                }
            )
        }

        // Màn hình Schedule
        composable(Screen.Schedule.route) {
            ScheduleScreen(
                viewModel = scheduleViewModel
            )
        }

        // Màn hình Control với tham số mode
        composable(
            route = Screen.Control.route,
            arguments = listOf(
                navArgument("mode") {
                    type = NavType.StringType
                    defaultValue = "peak"
                }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "peak"
            ControlScreen(
                viewModel = controlViewModel,
                username = username,
                initialTab = mode
            )
        }

        // Màn hình History
        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = historyViewModel
            )
        }

        // Màn hình Settings
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = dashboardViewModel,
                username = username
            )
        }
    }
}