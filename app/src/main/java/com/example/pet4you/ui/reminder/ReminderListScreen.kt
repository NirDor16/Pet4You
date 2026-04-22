package com.example.pet4you.ui.reminder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pet4you.data.model.Reminder
import com.example.pet4you.data.model.ReminderStatus
import com.example.pet4you.data.model.ReminderType
import com.example.pet4you.viewmodel.ReminderActionState
import com.example.pet4you.viewmodel.ReminderListState
import com.example.pet4you.viewmodel.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ReminderViewModel = viewModel()
) {
    val reminderListState by viewModel.reminderListState.collectAsState()
    val reminderActionState by viewModel.reminderActionState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadReminders() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Reminders") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (reminderActionState is ReminderActionState.Error) {
                Text(
                    text = (reminderActionState as ReminderActionState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            when (val state = reminderListState) {
                is ReminderListState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ReminderListState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = Color.Red)
                    }
                }
                is ReminderListState.Success -> {
                    if (state.reminders.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No reminders yet...")
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.reminders) { reminder ->
                                ReminderCard(
                                    reminder = reminder,
                                    dogName = state.dogMap[reminder.dogId] ?: "Unknown dog",
                                    onEdit = { onNavigateToEdit(reminder.reminderId) },
                                    onDelete = { viewModel.deleteReminder(reminder.reminderId) },
                                    onToggleStatus = { viewModel.toggleStatus(reminder) }
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
private fun ReminderCard(
    reminder: Reminder,
    dogName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleStatus: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ReminderType.displayName(reminder.type),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = dogName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = dateFormatter.format(Date(reminder.dateTime)),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = reminder.status == ReminderStatus.ACTIVE,
                        onClick = onToggleStatus,
                        label = {
                            Text(if (reminder.status == ReminderStatus.ACTIVE) "ACTIVE" else "DONE")
                        }
                    )
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }
}
