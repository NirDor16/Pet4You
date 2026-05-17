package com.example.pet4you.ui.meetup

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import com.example.pet4you.data.model.Meetup
import com.example.pet4you.ui.components.EmptyState
import com.example.pet4you.ui.components.ErrorMessage
import com.example.pet4you.ui.components.LoadingBox
import com.example.pet4you.ui.components.Pet4YouTopBar
import com.example.pet4you.viewmodel.MeetupActionState
import com.example.pet4you.viewmodel.MeetupListState
import com.example.pet4you.viewmodel.MeetupViewModel
import com.example.pet4you.viewmodel.RecommendState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MeetupListScreen(
    onNavigateToCreate: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: MeetupViewModel = viewModel(),
) {
    val meetupListState   by viewModel.meetupListState.collectAsState()
    val meetupActionState by viewModel.meetupActionState.collectAsState()
    val recommendState    by viewModel.recommendState.collectAsState()
    var selectedTab       by remember { mutableStateOf(0) }
    val tabs = listOf("All Meetups", "For You")

    LaunchedEffect(Unit) { viewModel.loadMeetups() }
    LaunchedEffect(meetupActionState) {
        if (meetupActionState is MeetupActionState.Success) viewModel.resetActionState()
    }
    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) viewModel.loadRecommendations()
    }

    Scaffold(
        topBar = { Pet4YouTopBar(title = "Meetups", onBack = onNavigateBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Meetup")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick  = { selectedTab = index },
                        text     = { Text(title, style = MaterialTheme.typography.labelLarge) },
                    )
                }
            }

            if (meetupActionState is MeetupActionState.Error) {
                ErrorMessage(
                    message  = (meetupActionState as MeetupActionState.Error).message,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            if (selectedTab == 0) {
                when (val state = meetupListState) {
                    is MeetupListState.Loading -> LoadingBox()
                    is MeetupListState.Error   -> ErrorMessage(state.message)
                    is MeetupListState.Success -> {
                        if (state.meetups.isEmpty()) {
                            EmptyState(
                                icon     = Icons.Filled.Group,
                                title    = "No meetups yet",
                                subtitle = "Tap + to create the first one!",
                            )
                        } else {
                            LazyColumn(
                                contentPadding      = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                items(state.meetups, key = { it.meetupId }) { meetup ->
                                    MeetupCard(
                                        meetup        = meetup,
                                        currentUserId = state.currentUserId,
                                        onJoin        = { viewModel.joinMeetup(meetup.meetupId) },
                                        onLeave       = { viewModel.leaveMeetup(meetup.meetupId) },
                                        onDelete      = { viewModel.deleteMeetup(meetup.meetupId) },
                                    )
                                }
                            }
                        }
                    }
                    else -> {}
                }
            } else {
                when (val state = recommendState) {
                    is RecommendState.Loading, RecommendState.Idle -> LoadingBox()
                    is RecommendState.Error -> ErrorMessage(state.message)
                    is RecommendState.Success -> {
                        if (state.meetups.isEmpty()) {
                            EmptyState(
                                icon     = Icons.Filled.Group,
                                title    = "No recommendations yet",
                                subtitle = "Add your dog's breed to see matching meetups",
                            )
                        } else {
                            LazyColumn(
                                contentPadding      = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                items(state.meetups, key = { it.meetupId }) { meetup ->
                                    MeetupCard(
                                        meetup        = meetup,
                                        currentUserId = state.currentUserId,
                                        onJoin        = { viewModel.joinMeetup(meetup.meetupId) },
                                        onLeave       = { viewModel.leaveMeetup(meetup.meetupId) },
                                        onDelete      = { viewModel.deleteMeetup(meetup.meetupId) },
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
}

@Composable
private fun MeetupCard(
    meetup: Meetup,
    currentUserId: String?,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onDelete: () -> Unit,
) {
    val dateFormatter  = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val isCreator      = meetup.creatorId == currentUserId
    val isParticipant  = currentUserId != null && meetup.participants.contains(currentUserId)

    ElevatedCard(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(meetup.location, style = MaterialTheme.typography.titleSmall)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Filled.CalendarToday,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier.size(13.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text  = dateFormatter.format(Date(meetup.dateTime)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (meetup.description.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text  = meetup.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Filled.Group,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(13.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text  = "${meetup.participants.size} participant(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                if (isCreator) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

            if (!isCreator) {
                if (isParticipant) {
                    OutlinedButton(onClick = onLeave, modifier = Modifier.fillMaxWidth()) { Text("Leave Meetup") }
                } else {
                    Button(onClick = onJoin, modifier = Modifier.fillMaxWidth()) { Text("Join Meetup") }
                }
            } else {
                Text(
                    text  = "Your meetup",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
