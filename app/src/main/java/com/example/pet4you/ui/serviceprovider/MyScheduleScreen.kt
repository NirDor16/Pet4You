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
import com.example.pet4you.data.model.ServiceRequest
import com.example.pet4you.viewmodel.MyScheduleViewModel
import com.example.pet4you.viewmodel.ScheduleState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyScheduleScreen(
    onBack: () -> Unit,
    viewModel: MyScheduleViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadSchedule() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Schedule") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            when (val s = state) {
                is ScheduleState.Loading, ScheduleState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ScheduleState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(s.message, color = Color.Red)
                    }
                }
                is ScheduleState.Success -> {
                    if (s.requests.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No scheduled appointments")
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(s.requests, key = { it.requestId }) { request ->
                                ScheduleCard(
                                    request = request,
                                    ownerName = s.ownerMap[request.dogOwnerId] ?: request.dogOwnerId,
                                    dogName = s.dogMap[request.dogId] ?: request.dogId
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
private fun ScheduleCard(
    request: ServiceRequest,
    ownerName: String,
    dogName: String
) {
    val dateStr = remember(request.scheduledAt) {
        if (request.scheduledAt > 0L) {
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                .format(Date(request.scheduledAt))
        } else {
            "No date set"
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "$dogName — $ownerName",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = request.providerType,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "📅 $dateStr",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
