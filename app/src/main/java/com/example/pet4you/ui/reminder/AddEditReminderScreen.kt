package com.example.pet4you.ui.reminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pet4you.data.model.ReminderFrequency
import com.example.pet4you.data.model.ReminderStatus
import com.example.pet4you.data.model.ReminderType
import com.example.pet4you.viewmodel.ReminderActionState
import com.example.pet4you.viewmodel.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddEditReminderScreen(
    reminderId: String?,
    onNavigateBack: () -> Unit,
    viewModel: ReminderViewModel = viewModel()
) {
    val isEditMode = !reminderId.isNullOrEmpty()
    val reminderActionState by viewModel.reminderActionState.collectAsState()
    val dogs by viewModel.dogs.collectAsState()

    var selectedDogId by remember { mutableStateOf("") }
    var selectedDogName by remember { mutableStateOf("Select dog") }
    var selectedType by remember { mutableStateOf(ReminderType.MEDICATION) }
    var selectedFrequency by remember { mutableStateOf(ReminderFrequency.ONCE) }
    var selectedStatus by remember { mutableStateOf(ReminderStatus.ACTIVE) }
    var selectedDateTime by remember { mutableStateOf(System.currentTimeMillis()) }

    var dogDropdownExpanded by remember { mutableStateOf(false) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var frequencyDropdownExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    LaunchedEffect(Unit) { viewModel.loadDogs() }

    LaunchedEffect(reminderId) {
        if (isEditMode) viewModel.loadReminder(reminderId!!)
    }

    LaunchedEffect(reminderActionState) {
        when (val state = reminderActionState) {
            is ReminderActionState.ReminderLoaded -> {
                val r = state.reminder
                selectedDogId = r.dogId
                selectedType = r.type
                selectedFrequency = r.frequency
                selectedStatus = r.status
                selectedDateTime = r.dateTime
            }
            is ReminderActionState.Success -> {
                viewModel.resetActionState()
                onNavigateBack()
            }
            else -> {}
        }
    }

    LaunchedEffect(dogs, selectedDogId) {
        if (selectedDogId.isNotEmpty()) {
            val name = dogs.find { it.dogId == selectedDogId }?.name
            if (name != null) selectedDogName = name
        }
    }

    val isLoading = reminderActionState is ReminderActionState.Loading
    val errorMessage = (reminderActionState as? ReminderActionState.Error)?.message

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isEditMode) "Edit Reminder" else "Add Reminder",
            style = MaterialTheme.typography.headlineSmall
        )

        // Dog dropdown
        Box {
            OutlinedButton(
                onClick = { dogDropdownExpanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedDogName, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = dogDropdownExpanded,
                onDismissRequest = { dogDropdownExpanded = false }
            ) {
                dogs.forEach { dog ->
                    DropdownMenuItem(
                        text = { Text(dog.name) },
                        onClick = {
                            selectedDogId = dog.dogId
                            selectedDogName = dog.name
                            dogDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Type dropdown
        Box {
            OutlinedButton(
                onClick = { typeDropdownExpanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Type: ${ReminderType.displayName(selectedType)}", modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = typeDropdownExpanded,
                onDismissRequest = { typeDropdownExpanded = false }
            ) {
                ReminderType.all.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(ReminderType.displayName(type)) },
                        onClick = {
                            selectedType = type
                            typeDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Date and time picker
        OutlinedButton(
            onClick = {
                val cal = Calendar.getInstance().apply { timeInMillis = selectedDateTime }
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                cal.set(year, month, day, hour, minute, 0)
                                selectedDateTime = cal.timeInMillis
                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Date & Time: ${dateFormatter.format(Date(selectedDateTime))}")
        }

        // Frequency dropdown
        Box {
            OutlinedButton(
                onClick = { frequencyDropdownExpanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Frequency: ${ReminderFrequency.displayName(selectedFrequency)}", modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = frequencyDropdownExpanded,
                onDismissRequest = { frequencyDropdownExpanded = false }
            ) {
                ReminderFrequency.all.forEach { freq ->
                    DropdownMenuItem(
                        text = { Text(ReminderFrequency.displayName(freq)) },
                        onClick = {
                            selectedFrequency = freq
                            frequencyDropdownExpanded = false
                        }
                    )
                }
            }
        }

        if (errorMessage != null) {
            Text(errorMessage, color = Color.Red)
        }

        Button(
            onClick = {
                if (isEditMode) {
                    viewModel.updateReminder(
                        reminderId!!, selectedDogId, selectedType,
                        selectedDateTime, selectedFrequency, selectedStatus
                    )
                } else {
                    viewModel.addReminder(selectedDogId, selectedType, selectedDateTime, selectedFrequency)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && selectedDogId.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            } else {
                Text(if (isEditMode) "Save Changes" else "Add Reminder")
            }
        }

        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}
