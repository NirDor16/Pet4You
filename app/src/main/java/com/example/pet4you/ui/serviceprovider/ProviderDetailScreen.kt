package com.example.pet4you.ui.serviceprovider

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pet4you.data.model.Dog
import com.example.pet4you.data.model.ProviderType
import com.example.pet4you.data.model.RequestStatus
import com.example.pet4you.data.model.ServiceProvider
import com.example.pet4you.data.model.ServiceRequest
import com.example.pet4you.ui.components.ErrorMessage
import com.example.pet4you.ui.components.InfoRow
import com.example.pet4you.ui.components.LoadingBox
import com.example.pet4you.ui.components.PawBackground
import com.example.pet4you.ui.components.Pet4YouTopBar
import com.example.pet4you.ui.components.StatusBadge
import com.example.pet4you.viewmodel.ProviderDetailState
import com.example.pet4you.viewmodel.ProviderDetailViewModel
import com.example.pet4you.viewmodel.SendRequestState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDetailScreen(
    providerId: String,
    onNavigateBack: () -> Unit,
    viewModel: ProviderDetailViewModel = viewModel(),
) {
    val detailState      by viewModel.detailState.collectAsState()
    val sendRequestState by viewModel.sendRequestState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(providerId) { viewModel.load(providerId) }
    LaunchedEffect(sendRequestState) {
        if (sendRequestState is SendRequestState.Success) { showDialog = false; viewModel.resetSendState() }
    }

    Scaffold(
        topBar = { Pet4YouTopBar(title = "Provider Profile", onBack = onNavigateBack) },
    ) { padding ->
        when (val s = detailState) {
            is ProviderDetailState.Loading, ProviderDetailState.Idle -> LoadingBox(modifier = Modifier.padding(padding))
            is ProviderDetailState.Error -> ErrorMessage(s.message, modifier = Modifier.padding(padding))
            is ProviderDetailState.Loaded -> {
                ProviderDetailContent(
                    provider          = s.provider,
                    dogs              = s.dogs,
                    existingRequest   = s.existingRequest,
                    padding           = padding,
                    onSendRequestClick = { showDialog = true },
                    onCancelRequest    = { s.existingRequest?.let { viewModel.cancelRequest(it.requestId) } },
                )
                if (showDialog) {
                    SendRequestDialog(
                        provider         = s.provider,
                        dogs             = s.dogs,
                        activeRequests   = s.activeRequests,
                        sendRequestState = sendRequestState,
                        onSend           = { dogId, message, scheduledAt ->
                            viewModel.sendRequest(s.provider.serviceProviderId, dogId, s.provider.providerType, message, scheduledAt)
                        },
                        onDismiss = { showDialog = false; viewModel.resetSendState() },
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun ProviderDetailContent(
    provider: ServiceProvider,
    dogs: List<Dog>,
    existingRequest: ServiceRequest?,
    padding: PaddingValues,
    onSendRequestClick: () -> Unit,
    onCancelRequest: () -> Unit,
) {
    PawBackground(modifier = Modifier.fillMaxSize().padding(padding)) {
    Column(
        modifier            = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ElevatedCard(
            modifier  = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text     = provider.fullName.ifEmpty { "Unnamed Provider" },
                        style    = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f),
                    )
                    Surface(
                        color        = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        shape        = MaterialTheme.shapes.extraSmall,
                    ) {
                        Text(
                            text     = ProviderType.displayName(provider.providerType),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style    = MaterialTheme.typography.labelMedium,
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                InfoRow(icon = Icons.Filled.Email, text = provider.email)
                if (provider.location.isNotEmpty()) {
                    InfoRow(icon = Icons.Filled.LocationOn, text = provider.location)
                }
                InfoRow(
                    icon = Icons.Filled.CheckCircle,
                    text = if (provider.isAvailable) "Available for new clients" else "Not currently available",
                    tint = if (provider.isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
            }
        }

        if (provider.description.isNotEmpty()) {
            ElevatedCard(
                modifier  = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoRow(icon = Icons.Filled.Notes, text = "About")
                    Text(
                        text  = provider.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        if (existingRequest != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text     = "You already sent a request to this provider",
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                StatusBadge(
                    label          = existingRequest.status,
                    containerColor = if (existingRequest.status == RequestStatus.APPROVED) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.tertiaryContainer
                    },
                    contentColor = if (existingRequest.status == RequestStatus.APPROVED) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    },
                )
            }
            if (existingRequest.scheduledAt > 0L) {
                Text(
                    text     = formatAppointmentRange(existingRequest.scheduledAt),
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            if (existingRequest.status == RequestStatus.APPROVED) {
                OutlinedButton(
                    onClick  = onCancelRequest,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Cancel Appointment") }
            }
            Spacer(Modifier.height(4.dp))
        }

        Button(
            onClick  = onSendRequestClick,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled  = dogs.isNotEmpty() && existingRequest == null,
        ) {
            Text(
                text  = when {
                    existingRequest != null -> "Request Already Sent"
                    dogs.isEmpty() -> "No dogs registered"
                    else -> "Send Service Request"
                },
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }       // closes Column(scroll+padding)
    }       // closes PawBackground
}

private val WORK_HOURS = 9..17 // slot start hours: 9:00..17:00 (9 one-hour slots, workday 9-18)

private fun localDayStart(utcMidnightMillis: Long): Long {
    val utcCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMidnightMillis }
    return Calendar.getInstance().apply {
        set(utcCal.get(Calendar.YEAR), utcCal.get(Calendar.MONTH), utcCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun slotMillis(dayStartMillis: Long, hour: Int): Long = Calendar.getInstance().apply {
    timeInMillis = dayStartMillis
    set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun isSameLocalDay(dayStartMillis: Long, otherMillis: Long): Boolean {
    val a = Calendar.getInstance().apply { timeInMillis = dayStartMillis }
    val b = Calendar.getInstance().apply { timeInMillis = otherMillis }
    return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
}

private fun formatAppointmentRange(scheduledAt: Long): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val start = Date(scheduledAt)
    val end = Date(scheduledAt + 3_600_000L)
    return "${dateFormat.format(start)}  ${timeFormat.format(start)}–${timeFormat.format(end)}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SendRequestDialog(
    provider: ServiceProvider,
    dogs: List<Dog>,
    activeRequests: List<ServiceRequest>,
    sendRequestState: SendRequestState,
    onSend: (dogId: String, message: String, scheduledAt: Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedDog        by remember { mutableStateOf<Dog?>(null) }
    var message            by remember { mutableStateOf("") }
    var dogDropdownExpanded by remember { mutableStateOf(false) }
    var selectedDayMillis   by remember { mutableStateOf<Long?>(null) }
    var selectedHour        by remember { mutableStateOf<Int?>(null) }
    var showDatePicker      by remember { mutableStateOf(false) }

    val isLoading    = sendRequestState is SendRequestState.Loading
    val errorMessage = (sendRequestState as? SendRequestState.Error)?.message

    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                localDayStart(utcTimeMillis) >= todayStart
        },
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selected = datePickerState.selectedDateMillis
                    if (selected != null) {
                        selectedDayMillis = localDayStart(selected)
                        selectedHour = null
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = datePickerState) }
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Send Request to ${provider.fullName.ifEmpty { "Provider" }}") },
        text = {
            Column(
                modifier            = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ExposedDropdownMenuBox(
                    expanded         = dogDropdownExpanded,
                    onExpandedChange = { dogDropdownExpanded = it },
                ) {
                    OutlinedTextField(
                        value        = selectedDog?.name ?: "",
                        onValueChange = {},
                        readOnly     = true,
                        label        = { Text("Select Dog") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dogDropdownExpanded) },
                        modifier     = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded         = dogDropdownExpanded,
                        onDismissRequest = { dogDropdownExpanded = false },
                    ) {
                        dogs.forEach { dog ->
                            DropdownMenuItem(
                                text    = { Text("${dog.name} (${dog.breed})") },
                                onClick = { selectedDog = dog; dogDropdownExpanded = false },
                            )
                        }
                    }
                }

                Text(text = "Choose a day and time", style = MaterialTheme.typography.labelLarge)

                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        selectedDayMillis?.let {
                            SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date(it))
                        } ?: "Select a date",
                    )
                }

                if (selectedDayMillis != null) {
                    val day = selectedDayMillis!!
                    val now = System.currentTimeMillis()
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(WORK_HOURS.toList()) { hour ->
                            val slot = slotMillis(day, hour)
                            val isTaken = activeRequests.any { it.scheduledAt == slot }
                            val isPast = isSameLocalDay(day, now) && slot <= now
                            FilterChip(
                                selected = selectedHour == hour,
                                onClick  = { selectedHour = hour },
                                enabled  = !isTaken && !isPast,
                                label    = { Text(String.format(Locale.getDefault(), "%02d:00", hour)) },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value         = message,
                    onValueChange = { message = it },
                    label         = { Text("Message (optional)") },
                    modifier      = Modifier.fillMaxWidth(),
                    minLines      = 2,
                )

                if (errorMessage != null) {
                    Text(
                        text  = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dog = selectedDog ?: return@Button
                    val day = selectedDayMillis ?: return@Button
                    val hour = selectedHour ?: return@Button
                    onSend(dog.dogId, message, slotMillis(day, hour))
                },
                enabled = !isLoading && selectedDog != null && selectedDayMillis != null && selectedHour != null,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Send")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Cancel") }
        },
    )
}
