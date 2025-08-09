package com.example.trafficlightcontrol.ui.screen.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.trafficlightcontrol.domain.model.User
import com.example.trafficlightcontrol.domain.model.UserRole
import com.example.trafficlightcontrol.navigation.Screen
import com.example.trafficlightcontrol.ui.component.AdminBottomNavigationBar
import com.example.trafficlightcontrol.ui.component.LoadingIndicator
import com.example.trafficlightcontrol.ui.viewmodel.admin.UserManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToConfiguration: () -> Unit,
    onNavigateToLogs: () -> Unit,
    viewModel: UserManagementViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showEditUserDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToDashboard) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddUserDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add User")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                AdminBottomNavigationBar(
                    currentRoute = Screen.UserManagement.route,
                    onNavigate = { route ->
                        when (route) {
                            Screen.AdminDashboard.route -> onNavigateToDashboard()
                            Screen.Configuration.route -> onNavigateToConfiguration()
                            Screen.Logs.route -> onNavigateToLogs()
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            LoadingIndicator(fullScreen = true)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (state.users.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "No users found.\nTap + to add one.",
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(state.users) { user ->
                            UserCard(
                                user = user,
                                onEdit = {
                                    selectedUser = user
                                    showEditUserDialog = true
                                },
                                onDelete = {
                                    selectedUser = user
                                    showDeleteConfirmDialog = true
                                },
                                onToggleActive = { viewModel.toggleUserActive(user) }
                            )
                        }
                    }
                }

                if (state.error != null) {
                    Snackbar(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(state.error)
                    }
                }
            }
        }
    }

    // Add User Dialog
    if (showAddUserDialog) {
        AddUserDialog(
            onDismiss = { showAddUserDialog = false },
            onAddUser = { email, password, name, role ->
                viewModel.createUser(email, password, name, role)
                showAddUserDialog = false
            }
        )
    }

    // Edit User Dialog
    if (showEditUserDialog && selectedUser != null) {
        EditUserDialog(
            user = selectedUser!!,
            onDismiss = { showEditUserDialog = false },
            onUpdateUser = { updatedUser ->
                viewModel.updateUser(updatedUser)
                showEditUserDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && selectedUser != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete the user ${selectedUser?.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        selectedUser?.let { viewModel.deleteUser(it.id) }
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("DELETE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
fun UserCard(
    user: User,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row {
                    Switch(
                        checked = user.isActive,
                        onCheckedChange = { onToggleActive() }
                    )
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Role: ",
                    style = MaterialTheme.typography.bodyMedium
                )

                val roleColor = if (user.role == UserRole.ADMIN)
                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

                Surface(
                    color = roleColor,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = user.role.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Status: ",
                    style = MaterialTheme.typography.bodyMedium
                )

                val statusColor = if (user.isActive)
                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                val statusText = if (user.isActive) "Active" else "Inactive"

                Surface(
                    color = statusColor,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onAddUser: (email: String, password: String, name: String, role: UserRole) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.OPERATOR) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New User") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                Text(
                    text = "Role",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = role == UserRole.ADMIN,
                        onClick = { role = UserRole.ADMIN }
                    )
                    Text(
                        text = "Admin",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = role == UserRole.OPERATOR,
                        onClick = { role = UserRole.OPERATOR }
                    )
                    Text(
                        text = "Operator",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAddUser(email, password, name, role) },
                enabled = email.isNotBlank() && password.isNotBlank() && name.isNotBlank()
            ) {
                Text("ADD USER")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserDialog(
    user: User,
    onDismiss: () -> Unit,
    onUpdateUser: (User) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var role by remember { mutableStateOf(user.role) }
    var isActive by remember { mutableStateOf(user.isActive) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit User") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                Text(
                    text = "Email: ${user.email}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Text(
                    text = "Role",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = role == UserRole.ADMIN,
                        onClick = { role = UserRole.ADMIN }
                    )
                    Text(
                        text = "Admin",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    RadioButton(
                        selected = role == UserRole.OPERATOR,
                        onClick = { role = UserRole.OPERATOR }
                    )
                    Text(
                        text = "Operator",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Text(
                        text = "Active Account",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedUser = user.copy(
                        name = name,
                        role = role,
                        isActive = isActive
                    )
                    onUpdateUser(updatedUser)
                },
                enabled = name.isNotBlank()
            ) {
                Text("UPDATE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}