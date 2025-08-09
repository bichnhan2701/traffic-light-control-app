package com.example.trafficlightcontrol.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trafficlightcontrol.domain.model.UserRole
import com.example.trafficlightcontrol.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    var state by mutableStateOf(LoginState())
        private set

    fun onEmailChange(email: String) {
        state = state.copy(
            email = email,
            emailError = null,
            errorMessage = null
        )
    }

    fun onPasswordChange(password: String) {
        state = state.copy(
            password = password,
            passwordError = null,
            errorMessage = null
        )
    }

    fun login() {
        if (!validateInputs()) return

        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            loginUseCase(state.email, state.password)
                .onSuccess { user ->
                    state = state.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        userRole = user.role
                    )
                }
                .onFailure { exception ->
                    state = state.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Authentication failed"
                    )
                }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (state.email.isBlank()) {
            state = state.copy(emailError = "Email cannot be empty")
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            state = state.copy(emailError = "Please enter a valid email")
            isValid = false
        }

        if (state.password.isBlank()) {
            state = state.copy(passwordError = "Password cannot be empty")
            isValid = false
        } else if (state.password.length < 6) {
            state = state.copy(passwordError = "Password must be at least 6 characters")
            isValid = false
        }

        return isValid
    }
}

data class LoginState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null,
    val userRole: UserRole = UserRole.OPERATOR
)