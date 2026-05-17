package com.example.pet4you.ui.serviceprovider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.example.pet4you.data.model.ServiceRequest
import com.example.pet4you.ui.components.EmptyState
import com.example.pet4you.ui.components.ErrorMessage
import com.example.pet4you.ui.components.LoadingBox
import com.example.pet4you.ui.components.Pet4YouTopBar
import com.example.pet4you.viewmodel.MyScheduleViewModel
import com.example.pet4you.viewmodel.ScheduleState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyScheduleScreen(
    onBack: () -> Unit,
    viewModel: MyScheduleViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadSchedule() }

    Scaffold(
        topBar = { Pet4YouTopBar(title = "My Schedule", onBack = onBack) },
    ) { padding ->
        when (val s = state) {
            is ScheduleState.Loading, ScheduleState.Idle -> LoadingBox(modifier = Modifier.padding(padding))
            is ScheduleState.Error -> ErrorMessage(s.message, modifier = Modifier.padding(padding))
            is ScheduleState.Success -> {
                if (s.requests.isEmpty()) {
                    EmptyState(
                        icon     = Icons.Filled.CalendarMonth,
                        title    = "No scheduled appointments",
                        subtitle = "Approved requests will appear here",
                        modifier = Modifier.padding(padding),
                    )
                } else {
                    LazyColumn(
                        modifier            = Modifier.padding(padding),
                        contentPadding      = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(s.requests, key = { it.requestId }) { request ->
                            ScheduleCard(
                                request   = request,
                                ownerName = s.ownerMap[request.dogOwnerId] ?: request.dogOwnerId,
                                dogName   = s.dogMap[request.dogId] ?: request.dogId,
                            )
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun ScheduleCard(
    request: ServiceRequest,
    ownerName: String,
    dogName: String,
) {
    val dateStr = remember(request.scheduledAt) {
        if (request.scheduledAt > 0L) {
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(request.scheduledAt))
        } else {
            "No date set"
        }
    }

    ElevatedCard(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                    ),
            )
            Column(
                modifier            = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text  = "$dogName — $ownerName",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text  = request.providerType,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color    = MaterialTheme.colorScheme.outlineVariant,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        modifier           = Modifier.size(14.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text  = dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
