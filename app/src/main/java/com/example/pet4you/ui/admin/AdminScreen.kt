package com.example.pet4you.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pet4you.data.model.User
import com.example.pet4you.ui.components.EmptyState
import com.example.pet4you.ui.components.ErrorMessage
import com.example.pet4you.ui.components.LoadingBox
import com.example.pet4you.ui.components.Pet4YouTopBar
import com.example.pet4you.viewmodel.AdminActionState
import com.example.pet4you.viewmodel.AdminState
import com.example.pet4you.viewmodel.AdminViewModel

@Composable
fun AdminScreen(
    onLogout: () -> Unit,
    viewModel: AdminViewModel = viewModel(),
) {
    val state       by viewModel.state.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadUsers() }
    LaunchedEffect(actionState) {
        if (actionState is AdminActionState.Success) viewModel.resetActionState()
    }

    Scaffold(
        topBar = {
            Pet4YouTopBar(
                title   = "Admin Panel",
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (actionState is AdminActionState.Error) {
                ErrorMessage(
                    message  = (actionState as AdminActionState.Error).message,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            when (val s = state) {
                is AdminState.Loading, AdminState.Idle -> LoadingBox()
                is AdminState.Error -> ErrorMessage(s.message)
                is AdminState.Success -> {
                    if (s.users.isEmpty()) {
                        EmptyState(
                            icon     = Icons.Filled.Group,
                            title    = "No users found",
                            subtitle = "Users will appear here",
                        )
                    } else {
                        LazyColumn(
                            contentPadding      = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(s.users, key = { it.uid }) { user ->
                                UserCard(
                                    user          = user,
                                    onToggleBlock = { viewModel.setBlocked(user.uid, !user.isBlocked) },
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
    val statusContainerColor = if (user.isBlocked) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val statusContentColor = if (user.isBlocked) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
    val statusText = if (user.isBlocked) "Blocked" else "Active"

    ElevatedCard(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(user.fullName, style = MaterialTheme.typography.titleSmall)
                Text(
                    user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    user.role,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Surface(
                    color        = statusContainerColor,
                    contentColor = statusContentColor,
                    shape        = MaterialTheme.shapes.extraSmall,
                ) {
                    Text(
                        text     = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style    = MaterialTheme.typography.labelSmall,
                    )
                }
                Spacer(Modifier.height(2.dp))
                if (user.isBlocked) {
                    Button(
                        onClick = onToggleBlock,
                        colors  = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor   = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) { Text("Unblock") }
                } else {
                    OutlinedButton(
                        onClick = onToggleBlock,
                        colors  = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) { Text("Block") }
                }
            }
        }
    }
}
