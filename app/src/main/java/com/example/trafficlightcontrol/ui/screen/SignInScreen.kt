package com.example.trafficlightcontrol.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trafficlightcontrol.R
import com.example.trafficlightcontrol.ui.component.LoadingIndicator
import com.example.trafficlightcontrol.ui.viewmodel.SignInViewModel

@Composable
fun SignInScreen(
    onLoginSuccess: () -> Unit,
    viewModel: SignInViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val context = LocalContext.current

    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = state.isAuthenticated) {
        if (state.isAuthenticated) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(key1 = state.errorMessage) {
        state.errorMessage?.let { error ->
            // Show error message
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
        ) {
            // App Logo
            Image(
                painter = painterResource(id = R.drawable.logo_traffic),
                contentDescription = "Traffic Light Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 24.dp)
            )

            // App Title
            Text(
                text = "Traffic Light Manager",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Email Field
            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = "Email")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                isError = state.emailError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            if (state.emailError != null) {
                Text(
                    text = state.emailError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, bottom = 8.dp)
                )
            }

            // Password Field
            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Password")
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                isError = state.passwordError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            if (state.passwordError != null) {
                Text(
                    text = state.passwordError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            Button(
                onClick = { viewModel.login() },
                enabled = !state.isLoading && state.email.isNotEmpty() && state.password.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Login", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        if (state.isLoading) {
            LoadingIndicator(fullScreen = true)
        }
    }
}