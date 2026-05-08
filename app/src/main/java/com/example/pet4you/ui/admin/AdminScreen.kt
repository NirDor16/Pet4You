package com.example.pet4you.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pet4you.data.model.User
import com.example.pet4you.viewmodel.AdminActionState
import com.example.pet4you.viewmodel.AdminState
import com.example.pet4you.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onLogout: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadUsers() }

    LaunchedEffect(actionState) {
        if (actionState is AdminActionState.Success) viewModel.resetActionState()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (actionState is AdminActionState.Error) {
                Text(
                    text = (actionState as AdminActionState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            when (val s = state) {
                is AdminState.Loading, AdminState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is AdminState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(s.message, color = Color.Red)
                    }
                }
                is AdminState.Success -> {
                    if (s.users.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No users found")
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(s.users, key = { it.uid }) { user ->
                                UserCard(
                                    user = user,
                                    onToggleBlock = { viewModel.setBlocked(user.uid, !user.isBlocked) }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun UserCard(user: User, onToggleBlock: () -> Unit) {
    val blockedColor = Color(0xFFF44336)
    val activeColor = Color(0xFF4CAF50)
    val statusColor = if (user.isBlocked) blockedColor else activeColor
    val statusText = if (user.isBlocked) "🚫 Blocked" else "● Active"

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(user.fullName, style = MaterialTheme.typography.titleSmall)
                Text(
                    user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    user.role,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                if (user.isBlocked) {
                    Button(
                        onClick = onToggleBlock,
                        colors = ButtonDefaults.buttonColors(containerColor = activeColor)
                    ) {
                        Text("Unblock")
                    }
                } else {
                    OutlinedButton(
                        onClick = onToggleBlock,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = blockedColor)
                    ) {
                        Text("Block")
                    }
                }
            }
        }
    }
}
