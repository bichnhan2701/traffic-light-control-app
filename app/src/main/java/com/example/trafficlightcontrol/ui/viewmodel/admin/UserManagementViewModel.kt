package com.example.trafficlightcontrol.ui.viewmodel.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trafficlightcontrol.domain.model.ActionLog
import com.example.trafficlightcontrol.domain.model.ActionType
import com.example.trafficlightcontrol.domain.model.User
import com.example.trafficlightcontrol.domain.model.UserRole
import com.example.trafficlightcontrol.domain.repo.LogRepository
import com.example.trafficlightcontrol.domain.repo.UserRepository
import com.example.trafficlightcontrol.domain.usecase.CreateUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val logRepository: LogRepository,
    private val createUserUseCase: CreateUserUseCase
) : ViewModel() {

    var state by mutableStateOf(UserManagementState())
        private set

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            userRepository.getAllUsers()
                .onSuccess { users ->
                    state = state.copy(
                        users = users,
                        isLoading = false
                    )
                }
                .onFailure {
                    state = state.copy(
                        error = it.message ?: "Failed to load users",
                        isLoading = false
                    )
                }
        }
    }

    fun createUser(email: String, password: String, name: String, role: UserRole) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            createUserUseCase(email, password, name, role)
                .onSuccess {
                    loadUsers()
                }
                .onFailure {
                    state = state.copy(
                        error = it.message ?: "Failed to create user",
                        isLoading = false
                    )
                }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val currentUser = userRepository.getCurrentUser().first()
                ?: run {
                    state = state.copy(
                        error = "Not authenticated",
                        isLoading = false
                    )
                    return@launch
                }

            userRepository.updateUser(user)
                .onSuccess {
                    // Log the action
                    logRepository.addLog(
                        ActionLog(
                            userId = currentUser.id,
                            userName = currentUser.name,
                            userRole = currentUser.role,
                            timestamp = LocalDateTime.now(),
                            actionType = ActionType.USER_MANAGEMENT,
                            description = "Updated user: ${user.name}",
                            previousState = "Role: ${user.role.name}, Active: ${user.isActive}"
                        )
                    )

                    loadUsers()
                }
                .onFailure {
                    state = state.copy(
                        error = it.message ?: "Failed to update user",
                        isLoading = false
                    )
                }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val currentUser = userRepository.getCurrentUser().first()
                ?: run {
                    state = state.copy(
                        error = "Not authenticated",
                        isLoading = false
                    )
                    return@launch
                }

            // First get the user to log details
            val userToDelete = state.users.find { it.id == userId }

            userRepository.deleteUser(userId)
                .onSuccess {
                    // Log the action
                    if (userToDelete != null) {
                        logRepository.addLog(
                            ActionLog(
                                userId = currentUser.id,
                                userName = currentUser.name,
                                userRole = currentUser.role,
                                timestamp = LocalDateTime.now(),
                                actionType = ActionType.USER_MANAGEMENT,
                                description = "Deleted user: ${userToDelete.name}",
                                previousState = "Email: ${userToDelete.email}, Role: ${userToDelete.role.name}"
                            )
                        )
                    }

                    loadUsers()
                }
                .onFailure {
                    state = state.copy(
                        error = it.message ?: "Failed to delete user",
                        isLoading = false
                    )
                }
        }
    }

    fun toggleUserActive(user: User) {
        val updatedUser = user.copy(isActive = !user.isActive)
        updateUser(updatedUser)
    }
}

data class UserManagementState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val error: String? = null
)