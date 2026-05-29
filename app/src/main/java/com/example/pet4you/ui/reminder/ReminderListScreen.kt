package com.example.pet4you.ui.reminder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pet4you.data.model.Reminder
import com.example.pet4you.data.model.ReminderStatus
import com.example.pet4you.data.model.ReminderType
import com.example.pet4you.ui.components.EmptyState
import com.example.pet4you.ui.components.ErrorMessage
import com.example.pet4you.ui.components.LoadingBox
import com.example.pet4you.ui.components.Pet4YouTopBar
import com.example.pet4you.viewmodel.ReminderActionState
import com.example.pet4you.viewmodel.ReminderListState
import com.example.pet4you.viewmodel.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReminderListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ReminderViewModel = viewModel(),
) {
    val reminderListState   by viewModel.reminderListState.collectAsState()
    val reminderActionState by viewModel.reminderActionState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadReminders() }

    Scaffold(
        topBar = { Pet4YouTopBar(title = "Reminders", onBack = onNavigateBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (reminderActionState is ReminderActionState.Error) {
                ErrorMessage(
                    message  = (reminderActionState as ReminderActionState.Error).message,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            when (val state = reminderListState) {
                is ReminderListState.Loading -> LoadingBox()
                is ReminderListState.Error   -> ErrorMessage(state.message)
                is ReminderListState.Success -> {
                    if (state.reminders.isEmpty()) {
                        EmptyState(
                            icon     = Icons.Filled.Notifications,
                            title    = "No reminders yet",
                            subtitle = "Tap + to add your first reminder",
                        )
                    } else {
                        LazyColumn(
                            contentPadding      = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(state.reminders, key = { it.reminderId }) { reminder ->
                                ReminderCard(
                                    reminder       = reminder,
                                    dogName        = state.dogMap[reminder.dogId] ?: "Unknown dog",
                                    onEdit         = { onNavigateToEdit(reminder.reminderId) },
                                    onDelete       = { viewModel.deleteReminder(reminder.reminderId) },
                                    onToggleStatus = { viewModel.toggleStatus(reminder) },
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
    onToggleStatus: () -> Unit,
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val isActive      = reminder.status == ReminderStatus.ACTIVE

    ElevatedCard(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector        = Icons.Filled.Notifications,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(22.dp),
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = ReminderType.displayName(reminder.type),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text  = dogName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Filled.CalendarToday,
                            contentDescription = null,
                            modifier           = Modifier.size(14.dp),
                            tint               = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text  = dateFormatter.format(Date(reminder.dateTime)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                FilterChip(
                    selected = isActive,
                    onClick  = onToggleStatus,
                    label    = { Text(if (isActive) "Active" else "Done") },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor    = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor        = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
