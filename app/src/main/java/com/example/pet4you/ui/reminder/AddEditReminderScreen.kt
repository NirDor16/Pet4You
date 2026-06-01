package com.example.pet4you.ui.reminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pet4you.data.model.ReminderFrequency
import com.example.pet4you.data.model.ReminderStatus
import com.example.pet4you.data.model.ReminderType
import com.example.pet4you.ui.components.PawBackground
import com.example.pet4you.ui.components.Pet4YouTopBar
import com.example.pet4you.viewmodel.ReminderActionState
import com.example.pet4you.viewmodel.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddEditReminderScreen(
    reminderId: String?,
    onNavigateBack: () -> Unit,
    viewModel: ReminderViewModel = viewModel(),
) {
    val isEditMode           = !reminderId.isNullOrEmpty()
    val reminderActionState  by viewModel.reminderActionState.collectAsState()
    val dogs                 by viewModel.dogs.collectAsState()

    var selectedDogId        by remember { mutableStateOf("") }
    var selectedDogName      by remember { mutableStateOf("Select dog") }
    var selectedType         by remember { mutableStateOf(ReminderType.MEDICATION) }
    var selectedFrequency    by remember { mutableStateOf(ReminderFrequency.ONCE) }
    var selectedStatus       by remember { mutableStateOf(ReminderStatus.ACTIVE) }
    var selectedDateTime     by remember { mutableStateOf(System.currentTimeMillis()) }
    var dogDropdownExpanded  by remember { mutableStateOf(false) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var freqDropdownExpanded by remember { mutableStateOf(false) }

    val context       = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    LaunchedEffect(Unit) { viewModel.loadDogs() }
    LaunchedEffect(reminderId) { if (isEditMode) viewModel.loadReminder(reminderId!!) }
    LaunchedEffect(reminderActionState) {
        when (val state = reminderActionState) {
            is ReminderActionState.ReminderLoaded -> {
                val r = state.reminder
                selectedDogId = r.dogId; selectedType = r.type
                selectedFrequency = r.frequency; selectedStatus = r.status
                selectedDateTime = r.dateTime
            }
            is ReminderActionState.Success -> { viewModel.resetActionState(); onNavigateBack() }
            else -> {}
        }
    }
    LaunchedEffect(dogs, selectedDogId) {
        if (selectedDogId.isNotEmpty()) {
            dogs.find { it.dogId == selectedDogId }?.name?.let { selectedDogName = it }
        }
    }

    val isLoading    = reminderActionState is ReminderActionState.Loading
    val errorMessage = (reminderActionState as? ReminderActionState.Error)?.message

    Scaffold(
        topBar = {
            Pet4YouTopBar(
                title  = if (isEditMode) "Edit Reminder" else "Add Reminder",
                onBack = onNavigateBack,
            )
        },
    ) { paddingValues ->
        PawBackground(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier            = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
            // Dog picker
            Box {
                OutlinedButton(onClick = { dogDropdownExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(selectedDogName, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(expanded = dogDropdownExpanded, onDismissRequest = { dogDropdownExpanded = false }) {
                    dogs.forEach { dog ->
                        DropdownMenuItem(
                            text    = { Text(dog.name) },
                            onClick = { selectedDogId = dog.dogId; selectedDogName = dog.name; dogDropdownExpanded = false },
                        )
                    }
                }
            }

            // Type picker
            Box {
                OutlinedButton(onClick = { typeDropdownExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Type: ${ReminderType.displayName(selectedType)}", modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(expanded = typeDropdownExpanded, onDismissRequest = { typeDropdownExpanded = false }) {
                    ReminderType.all.forEach { type ->
                        DropdownMenuItem(
                            text    = { Text(ReminderType.displayName(type)) },
                            onClick = { selectedType = type; typeDropdownExpanded = false },
                        )
                    }
                }
            }

            // Date/time picker
            OutlinedButton(
                onClick  = {
                    val cal = Calendar.getInstance().apply { timeInMillis = selectedDateTime }
                    DatePickerDialog(context, { _, year, month, day ->
                        TimePickerDialog(context, { _, hour, minute ->
                            cal.set(year, month, day, hour, minute, 0)
                            selectedDateTime = cal.timeInMillis
                        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                Text("  ${dateFormatter.format(Date(selectedDateTime))}")
            }

            // Frequency picker
            Box {
                OutlinedButton(onClick = { freqDropdownExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Frequency: ${ReminderFrequency.displayName(selectedFrequency)}", modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(expanded = freqDropdownExpanded, onDismissRequest = { freqDropdownExpanded = false }) {
                    ReminderFrequency.all.forEach { freq ->
                        DropdownMenuItem(
                            text    = { Text(ReminderFrequency.displayName(freq)) },
                            onClick = { selectedFrequency = freq; freqDropdownExpanded = false },
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick  = {
                    if (isEditMode) viewModel.updateReminder(reminderId!!, selectedDogId, selectedType, selectedDateTime, selectedFrequency, selectedStatus)
                    else viewModel.addReminder(selectedDogId, selectedType, selectedDateTime, selectedFrequency)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled  = !isLoading && selectedDogId.isNotEmpty(),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(if (isEditMode) "Save Changes" else "Add Reminder", style = MaterialTheme.typography.labelLarge)
                }
            }

            OutlinedButton(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
        }   // closes PawBackground
    }
}
