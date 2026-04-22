package com.example.pet4you.ui.meetup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pet4you.data.model.Meetup
import com.example.pet4you.viewmodel.MeetupActionState
import com.example.pet4you.viewmodel.MeetupListState
import com.example.pet4you.viewmodel.MeetupViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetupListScreen(
    onNavigateToCreate: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: MeetupViewModel = viewModel()
) {
    val meetupListState by viewModel.meetupListState.collectAsState()
    val meetupActionState by viewModel.meetupActionState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadMeetups() }

    LaunchedEffect(meetupActionState) {
        if (meetupActionState is MeetupActionState.Success) {
            viewModel.resetActionState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Meetups") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate) {
                Icon(Icons.Default.Add, contentDescription = "Create Meetup")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (meetupActionState is MeetupActionState.Error) {
                Text(
                    text = (meetupActionState as MeetupActionState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            when (val state = meetupListState) {
                is MeetupListState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is MeetupListState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = Color.Red)
                    }
                }
                is MeetupListState.Success -> {
                    if (state.meetups.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No meetups yet. Create one!")
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.meetups) { meetup ->
                                MeetupCard(
                                    meetup = meetup,
                                    currentUserId = state.currentUserId,
                                    onJoin = { viewModel.joinMeetup(meetup.meetupId) },
                                    onLeave = { viewModel.leaveMeetup(meetup.meetupId) },
                                    onDelete = { viewModel.deleteMeetup(meetup.meetupId) }
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
private fun MeetupCard(
    meetup: Meetup,
    currentUserId: String?,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val isCreator = meetup.creatorId == currentUserId
    val isParticipant = currentUserId != null && meetup.participants.contains(currentUserId)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = meetup.location,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = dateFormatter.format(Date(meetup.dateTime)),
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (meetup.description.isNotEmpty()) {
                        Text(
                            text = meetup.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${meetup.participants.size} participant(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (isCreator) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!isCreator) {
                if (isParticipant) {
                    OutlinedButton(
                        onClick = onLeave,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Leave Meetup")
                    }
                } else {
                    Button(
                        onClick = onJoin,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Join Meetup")
                    }
                }
            } else {
                Text(
                    text = "Your meetup",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
