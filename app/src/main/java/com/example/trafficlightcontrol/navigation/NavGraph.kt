package com.example.trafficlightcontrol.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trafficlightcontrol.domain.model.UserRole
import com.example.trafficlightcontrol.domain.usecase.LogoutUseCase
import com.example.trafficlightcontrol.ui.screen.SignInScreen
import com.example.trafficlightcontrol.ui.screen.admin.ConfigurationScreen
import com.example.trafficlightcontrol.ui.screen.admin.LogsScreen
import com.example.trafficlightcontrol.ui.screen.admin.UserManagementScreen
import com.example.trafficlightcontrol.ui.screen.admin.AdminDashboardScreen
import com.example.trafficlightcontrol.ui.screen.operator.OperatorHistoryScreen
import com.example.trafficlightcontrol.ui.screen.operator.OperatorTrafficStatusScreen
import com.example.trafficlightcontrol.ui.viewmodel.SignInViewModel
import androidx.compose.runtime.rememberCoroutineScope
import com.example.trafficlightcontrol.ui.screen.BluetoothDeviceScreen
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    logoutUseCase: LogoutUseCase
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth
        composable(Screen.Login.route) {
            val loginViewModel = hiltViewModel<SignInViewModel>()
            var navigated by remember { mutableStateOf(false) }

            LaunchedEffect(loginViewModel.state.isAuthenticated, loginViewModel.state.userRole) {
                if (loginViewModel.state.isAuthenticated && !navigated) {
                    navigated = true

                    val destination = if (loginViewModel.state.userRole == UserRole.ADMIN) {
                        Screen.AdminDashboard.route
                    } else {
                        Screen.OperatorTrafficStatus.route
                    }

                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }

            SignInScreen(
                onLoginSuccess = { /* handled in LaunchedEffect */ }
            )
        }

        // Admin screens
        composable(Screen.AdminDashboard.route) {
            val coroutineScope = rememberCoroutineScope()
            AdminDashboardScreen(
                onNavigateToConfiguration = { navController.navigate(Screen.Configuration.route) },
                onNavigateToUserManagement = { navController.navigate(Screen.UserManagement.route) },
                onNavigateToLogs = { navController.navigate(Screen.Logs.route) },
                onNavigateToBluetooth = { navController.navigate(Screen.BluetoothDevice.route) },
                onLogout = {
                    coroutineScope.launch {
                        performLogout(navController, logoutUseCase)
                    }
                }
            )
        }

        composable(Screen.Configuration.route) {
            ConfigurationScreen(
                onNavigateToDashboard = { navController.navigate(Screen.AdminDashboard.route) {
                    popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                } },
                onNavigateToUserManagement = { navController.navigate(Screen.UserManagement.route) },
                onNavigateToLogs = { navController.navigate(Screen.Logs.route) },
                onNavigateToBluetooth = { navController.navigate(Screen.BluetoothDevice.route) }
            )
        }

        composable(Screen.UserManagement.route) {
            UserManagementScreen(
                onNavigateToDashboard = { navController.navigate(Screen.AdminDashboard.route) {
                    popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                } },
                onNavigateToConfiguration = { navController.navigate(Screen.Configuration.route) },
                onNavigateToLogs = { navController.navigate(Screen.Logs.route) }
            )
        }

        composable(Screen.Logs.route) {
            LogsScreen(
                onNavigateToDashboard = { navController.navigate(Screen.AdminDashboard.route) {
                    popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                } },
                onNavigateToConfiguration = { navController.navigate(Screen.Configuration.route) },
                onNavigateToUserManagement = { navController.navigate(Screen.UserManagement.route) }
            )
        }

        // Operator screens
        composable(Screen.OperatorTrafficStatus.route) {
            val coroutineScope = rememberCoroutineScope()
            OperatorTrafficStatusScreen(
                onNavigateToHistory = { navController.navigate(Screen.OperatorHistory.route) },
                onNavigateToBluetooth = { navController.navigate(Screen.BluetoothDevice.route) },
                onLogout = {
                    coroutineScope.launch {
                        performLogout(navController, logoutUseCase)
                    }
                }
            )
        }

        composable(Screen.OperatorHistory.route) {
            OperatorHistoryScreen(
                onNavigateBack = { navController.navigate(Screen.OperatorTrafficStatus.route) {
                    popUpTo(Screen.OperatorTrafficStatus.route) { inclusive = true }
                } }
            )
        }

        composable(Screen.BluetoothDevice.route) {
            BluetoothDeviceScreen(
                onNavigateBack = { navController.popBackStack() },
                onConnectionEstablished = {
                    navController.popBackStack()
                    // Hiển thị thông báo kết nối thành công nếu muốn
                }
            )
        }
    }
}

private suspend fun performLogout(
    navController: NavHostController,
    logoutUseCase: LogoutUseCase
) {
    logoutUseCase.invoke()
    navController.navigate(Screen.Login.route) {
        popUpTo(0) { inclusive = true }
    }
}
