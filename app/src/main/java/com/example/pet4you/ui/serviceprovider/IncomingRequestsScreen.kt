package com.example.pet4you.ui.serviceprovider

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pet4you.data.model.RequestStatus
import com.example.pet4you.data.model.ServiceRequest
import com.example.pet4you.viewmodel.IncomingRequestsState
import com.example.pet4you.viewmodel.IncomingRequestsViewModel
import com.example.pet4you.viewmodel.RequestActionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomingRequestsScreen(
    onNavigateBack: () -> Unit,
    viewModel: IncomingRequestsViewModel = viewModel()
) {
    val listState by viewModel.listState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadRequests() }

    LaunchedEffect(actionState) {
        if (actionState is RequestActionState.Success) {
            viewModel.resetActionState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Service Requests") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            if (actionState is RequestActionState.Error) {
                Text(
                    text = (actionState as RequestActionState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            when (val s = listState) {
                is IncomingRequestsState.Loading, IncomingRequestsState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is IncomingRequestsState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(s.message, color = Color.Red)
                    }
                }
                is IncomingRequestsState.Success -> {
                    if (s.requests.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No service requests yet.")
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(s.requests) { request ->
                                RequestCard(
                                    request = request,
                                    ownerName = s.ownerMap[request.dogOwnerId] ?: request.dogOwnerId,
                                    dogName = s.dogMap[request.dogId] ?: request.dogId,
                                    onApprove = { viewModel.approveRequest(request.requestId) },
                                    onReject = { viewModel.rejectRequest(request.requestId) }
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
    onReject: () -> Unit
) {
    val statusColor = when (request.status) {
        RequestStatus.APPROVED -> Color(0xFF4CAF50)
        RequestStatus.REJECTED -> Color(0xFFF44336)
        else -> Color(0xFFFFC107)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "From: $ownerName",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Dog: $dogName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = request.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
            }

            if (request.message.isNotEmpty()) {
                Text(
                    text = request.message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (request.status == RequestStatus.PENDING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Approve")
                    }
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reject")
                    }
                }
            }
        }
    }
}
