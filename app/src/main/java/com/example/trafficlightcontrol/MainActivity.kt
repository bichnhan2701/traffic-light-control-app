package com.example.trafficlightcontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.trafficlightcontrol.domain.model.UserRole
import com.example.trafficlightcontrol.domain.repo.UserRepository
import com.example.trafficlightcontrol.domain.usecase.LogoutUseCase
import com.example.trafficlightcontrol.ui.theme.TrafficLightControlTheme
import com.example.trafficlightcontrol.navigation.NavGraph
import com.example.trafficlightcontrol.navigation.Screen
import com.example.trafficlightcontrol.util.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var logoutUseCase: LogoutUseCase

    private val permissionHelper by lazy { PermissionHelper(this) }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Kết quả có thể được xử lý ở đây nếu cần */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Yêu cầu quyền cần thiết khi khởi động ứng dụng
        requestBluetoothPermissions()

        setContent {
            TrafficLightControlTheme {
                val navController = rememberNavController()
                var startDestination by remember { mutableStateOf<String?>(null) }

                androidx.compose.runtime.LaunchedEffect(true) {
                    val user = userRepository.getCurrentUser().first()
                    startDestination = if (user != null) {
                        if (user.role == UserRole.ADMIN) {
                            Screen.AdminDashboard.route
                        } else {
                            Screen.OperatorTrafficStatus.route
                        }
                    } else {
                        Screen.Login.route
                    }
                }

                startDestination?.let {
                    NavGraph(
                        navController = navController,
                        startDestination = it,
                        logoutUseCase = logoutUseCase
                    )
                }
            }
        }

        // Ensure admin account is created on first launch
        lifecycleScope.launch {
            createInitialAdminAccount()
        }
    }

    private fun requestBluetoothPermissions() {
        // Chỉ yêu cầu quyền nếu chưa được cấp
        if (!permissionHelper.hasBluetoothPermissions()) {
            requestPermissionLauncher.launch(permissionHelper.getRequiredBluetoothPermissions())
        }
    }

    private suspend fun createInitialAdminAccount() {
        try {
            // First check if the admin account exists in Firebase Auth
            val currentUser = userRepository.getCurrentUser().first()
            if (currentUser?.role == UserRole.ADMIN) {
                android.util.Log.d("AuthDebug", "Admin account already exists and is logged in. Skipping creation.")
                return
            }

            // Then check if any admin exists in Firestore
            val users = userRepository.getAllUsers().getOrNull() ?: emptyList()
            val adminExists = users.any { it.role == UserRole.ADMIN }

            if (!adminExists) {
                android.util.Log.d("AuthDebug", "No admin account found. Attempting to create one.")
                try {
                    // Create admin account
                    userRepository.createUser(
                        email = "admin@example.com",
                        password = "admin123",
                        name = "Admin",
                        role = UserRole.ADMIN
                    )
                    android.util.Log.d("AuthDebug", "Admin account created successfully.")
                } catch (e: Exception) {
                    if (e.message?.contains("already in use") == true) {
                        android.util.Log.d("AuthDebug", "Admin account exists in Auth but not in Firestore. Creating Firestore document.")
                        // The user exists in Auth but not in Firestore, let's fix this
                        try {
                            // Log in as admin to get the UID
                            val loginResult = userRepository.login("admin@example.com", "admin123")
                            android.util.Log.d("AuthDebug", "Successfully logged in as admin to create Firestore document.")
                        } catch (e2: Exception) {
                            android.util.Log.e("AuthDebug", "Failed to log in as admin: ${e2.message}")
                        }
                    } else {
                        throw e
                    }
                }
            } else {
                android.util.Log.d("AuthDebug", "Admin account already exists in Firestore. Skipping creation.")
            }
        } catch (e: Exception) {
            // Handle error with more specific messages
            if (e.message?.contains("PERMISSION_DENIED") == true) {
                android.util.Log.e("AuthDebug", "Firestore permission denied. Please check your Firebase security rules.", e)
                // In a real app, you might want to show this to the user
                // Toast.makeText(this, "Database access denied. Please contact the administrator.", Toast.LENGTH_LONG).show()
            } else if (e.message?.contains("already in use") == true) {
                android.util.Log.w("AuthDebug", "Admin account already exists in Auth. This is expected if the account was created previously.")
            } else {
                android.util.Log.e("AuthDebug", "Error creating admin account: ${e.message}", e)
            }
        }
    }
}
