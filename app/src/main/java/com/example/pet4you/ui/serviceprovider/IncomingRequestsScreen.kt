package com.example.pet4you.ui.serviceprovider

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pet4you.data.model.RequestStatus
import com.example.pet4you.data.model.ServiceRequest
import com.example.pet4you.ui.components.EmptyState
import com.example.pet4you.ui.components.ErrorMessage
import com.example.pet4you.ui.components.LoadingBox
import com.example.pet4you.ui.components.Pet4YouTopBar
import com.example.pet4you.ui.components.StatusBadge
import com.example.pet4you.viewmodel.IncomingRequestsState
import com.example.pet4you.viewmodel.IncomingRequestsViewModel
import com.example.pet4you.viewmodel.RequestActionState
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomingRequestsScreen(
    onNavigateBack: () -> Unit,
    viewModel: IncomingRequestsViewModel = viewModel(),
) {
    val listState   by viewModel.listState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    var pendingApprovalId    by remember { mutableStateOf<String?>(null) }
    var showDatePicker       by remember { mutableStateOf(false) }
    val datePickerState      = rememberDatePickerState()
    var showTimePickerForDate by remember { mutableStateOf<Long?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.loadRequests() }
    LaunchedEffect(actionState) {
        if (actionState is RequestActionState.Success) viewModel.resetActionState()
    }
    LaunchedEffect(showTimePickerForDate) {
        val dateMillis = showTimePickerForDate ?: return@LaunchedEffect
        val approvalId = pendingApprovalId ?: return@LaunchedEffect
        val now = Calendar.getInstance()
        TimePickerDialog(context, { _, hour, minute ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = dateMillis
                set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            viewModel.approveRequest(approvalId, cal.timeInMillis)
            pendingApprovalId = null; showTimePickerForDate = null
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false; pendingApprovalId = null },
            confirmButton = {
                TextButton(onClick = {
                    val selected = datePickerState.selectedDateMillis
                    showDatePicker = false
                    if (selected != null) showTimePickerForDate = selected else pendingApprovalId = null
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false; pendingApprovalId = null }) { Text("Cancel") }
            },
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        topBar = { Pet4YouTopBar(title = "Service Requests", onBack = onNavigateBack) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (actionState is RequestActionState.Error) {
                ErrorMessage(
                    message  = (actionState as RequestActionState.Error).message,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            when (val s = listState) {
                is IncomingRequestsState.Loading, IncomingRequestsState.Idle -> LoadingBox()
                is IncomingRequestsState.Error -> ErrorMessage(s.message)
                is IncomingRequestsState.Success -> {
                    if (s.requests.isEmpty()) {
                        EmptyState(
                            icon     = Icons.Filled.List,
                            title    = "No service requests yet",
                            subtitle = "New requests will appear here",
                        )
                    } else {
                        LazyColumn(
                            contentPadding      = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(s.requests, key = { it.requestId }) { request ->
                                RequestCard(
                                    request   = request,
                                    ownerName = s.ownerMap[request.dogOwnerId] ?: request.dogOwnerId,
                                    dogName   = s.dogMap[request.dogId] ?: request.dogId,
                                    onApprove = { pendingApprovalId = request.requestId; showDatePicker = true },
                                    onReject  = { viewModel.rejectRequest(request.requestId) },
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
private fun RequestCard(
    request: ServiceRequest,
    ownerName: String,
    dogName: String,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    val (containerColor, contentColor) = when (request.status) {
        RequestStatus.APPROVED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        RequestStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    }

    ElevatedCard(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("From: $ownerName", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text  = "Dog: $dogName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusBadge(
                    label          = request.status,
                    containerColor = containerColor,
                    contentColor   = contentColor,
                )
            }

            if (request.message.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Text(text = request.message, style = MaterialTheme.typography.bodyMedium)
            }

            if (request.status == RequestStatus.PENDING) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(onClick = onApprove, modifier = Modifier.weight(1f)) { Text("Approve") }
                    OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) { Text("Reject") }
                }
            }
        }
    }
}
