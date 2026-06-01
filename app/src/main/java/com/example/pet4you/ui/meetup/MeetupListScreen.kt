package com.example.pet4you.ui.meetup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pet4you.data.model.Meetup
import com.example.pet4you.network.WeatherResponse
import com.example.pet4you.repository.WeatherRepository
import com.example.pet4you.ui.components.EmptyState
import com.example.pet4you.ui.components.ErrorMessage
import com.example.pet4you.ui.components.LoadingBox
import com.example.pet4you.ui.components.PawBackground
import com.example.pet4you.ui.components.Pet4YouTopBar
import com.example.pet4you.ui.components.SectionHero
import com.example.pet4you.ui.components.StatusBadge
import com.example.pet4you.ui.theme.DeepGreen
import com.example.pet4you.ui.theme.SoftGreen
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
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: MeetupViewModel = viewModel(),
) {
    val meetupListState   by viewModel.meetupListState.collectAsState()
    val meetupActionState by viewModel.meetupActionState.collectAsState()
    val recommendState    by viewModel.recommendState.collectAsState()
    var selectedTab       by remember { mutableStateOf(0) }
    var searchQuery       by remember { mutableStateOf("") }
    val tabs = listOf("All Meetups", "My Meetups", "Recommended")

    LaunchedEffect(Unit) { viewModel.loadMeetups() }
    LaunchedEffect(meetupActionState) {
        if (meetupActionState is MeetupActionState.Success) viewModel.resetActionState()
    }
    LaunchedEffect(selectedTab) {
        if (selectedTab == 2) viewModel.loadRecommendations()
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
        PawBackground(modifier = Modifier.fillMaxSize().padding(padding)) {
        Column(modifier = Modifier.fillMaxSize()) {
            SectionHero(
                icon           = Icons.Filled.Group,
                title          = "Meetups",
                subtitle       = "Find & join local meetups",
                containerColor = SoftGreen,
                iconTint       = DeepGreen,
            )
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick  = { selectedTab = index },
                        text     = { Text(title, style = MaterialTheme.typography.labelLarge) },
                    )
                }
            }

            if (selectedTab == 0) {
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder   = { Text("Search meetups...") },
                    leadingIcon   = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine    = true,
                    modifier      = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            if (meetupActionState is MeetupActionState.Error) {
                ErrorMessage(
                    message  = (meetupActionState as MeetupActionState.Error).message,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            when (selectedTab) {
                0 -> AllMeetupsTab(meetupListState, searchQuery, onNavigateToDetail)
                1 -> MyMeetupsTab(meetupListState, onNavigateToDetail)
                2 -> RecommendedTab(recommendState, onNavigateToDetail)
            }
        }
        }   // closes PawBackground
    }
}

@Composable
private fun AllMeetupsTab(
    state: MeetupListState,
    searchQuery: String,
    onNavigateToDetail: (String) -> Unit,
) {
    when (state) {
        is MeetupListState.Loading -> LoadingBox()
        is MeetupListState.Error   -> ErrorMessage(state.message)
        is MeetupListState.Success -> {
            val filtered = state.meetups.filter { m ->
                searchQuery.isBlank() ||
                m.title.contains(searchQuery, ignoreCase = true) ||
                m.location.contains(searchQuery, ignoreCase = true) ||
                m.description.contains(searchQuery, ignoreCase = true)
            }
            if (filtered.isEmpty()) {
                EmptyState(
                    icon     = Icons.Filled.Group,
                    title    = if (searchQuery.isNotBlank()) "No results for \"$searchQuery\"" else "No meetups yet",
                    subtitle = if (searchQuery.isNotBlank()) "Try a different search" else "Tap + to create the first one!",
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filtered, key = { it.meetupId }) { meetup ->
                        MeetupCard(meetup = meetup, currentUserId = state.currentUserId, onClick = { onNavigateToDetail(meetup.meetupId) })
                    }
                }
            }
        }
        else -> {}
    }
}

@Composable
private fun MyMeetupsTab(
    state: MeetupListState,
    onNavigateToDetail: (String) -> Unit,
) {
    when (state) {
        is MeetupListState.Loading -> LoadingBox()
        is MeetupListState.Error   -> ErrorMessage(state.message)
        is MeetupListState.Success -> {
            val myMeetups = state.meetups.filter { m ->
                m.creatorId == state.currentUserId ||
                (state.currentUserId != null && state.currentUserId in m.participants)
            }
            if (myMeetups.isEmpty()) {
                EmptyState(
                    icon     = Icons.Filled.Group,
                    title    = "No meetups yet",
                    subtitle = "Join or create a meetup to see it here",
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(myMeetups, key = { it.meetupId }) { meetup ->
                        MeetupCard(meetup = meetup, currentUserId = state.currentUserId, onClick = { onNavigateToDetail(meetup.meetupId) })
                    }
                }
            }
        }
        else -> {}
    }
}

@Composable
private fun RecommendedTab(
    state: RecommendState,
    onNavigateToDetail: (String) -> Unit,
) {
    when (state) {
        is RecommendState.Loading, RecommendState.Idle -> LoadingBox()
        is RecommendState.Error   -> ErrorMessage(state.message)
        is RecommendState.Success -> {
            if (state.meetups.isEmpty()) {
                EmptyState(
                    icon     = Icons.Filled.Group,
                    title    = "No recommendations yet",
                    subtitle = "Add your dog's breed to see matching meetups",
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.meetups, key = { it.meetupId }) { meetup ->
                        MeetupCard(meetup = meetup, currentUserId = state.currentUserId, onClick = { onNavigateToDetail(meetup.meetupId) })
                    }
                }
            }
        }
        else -> {}
    }
}

@Composable
private fun MeetupCard(
    meetup: Meetup,
    currentUserId: String?,
    onClick: () -> Unit,
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val isCreator     = meetup.creatorId == currentUserId
    val isParticipant = currentUserId != null && currentUserId in meetup.participants

    ElevatedCard(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier         = Modifier
                    .size(46.dp)
                    .background(SoftGreen, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Group, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        text     = meetup.title.ifEmpty { "Meetup" },
                        style    = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    when {
                        isCreator     -> StatusBadge("Mine", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                        isParticipant -> StatusBadge("Joined", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
                        meetup.recommendationScore != null -> StatusBadge(
                            label          = "${(meetup.recommendationScore!! * 100).toInt()}% match",
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor   = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(13.dp))
                    Text("  ${meetup.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(13.dp))
                    Text("  ${dateFormatter.format(Date(meetup.dateTime))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Group, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(13.dp))
                        val participantText = if (meetup.participantLimit > 0)
                            "  ${meetup.participants.size}/${meetup.participantLimit}"
                        else "  ${meetup.participants.size} joining"
                        Text(participantText, style = MaterialTheme.typography.bodySmall, color = DeepGreen)
                    }
                    val daysUntil = (meetup.dateTime - System.currentTimeMillis()) / 86_400_000
                    if (daysUntil in 0..5 && meetup.location.isNotBlank()) {
                        WeatherChip(location = meetup.location, datetimeMs = meetup.dateTime)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherChip(location: String, datetimeMs: Long) {
    val weather by produceState<WeatherResponse?>(null, location, datetimeMs) {
        value = WeatherRepository.getWeatherForDate(location, datetimeMs)
    }
    weather?.let { w ->
        val condition = w.weather.firstOrNull()
        val iconUrl   = condition?.icon?.let { "https://openweathermap.org/img/wn/$it.png" }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (iconUrl != null) {
                AsyncImage(
                    model              = iconUrl,
                    contentDescription = null,
                    modifier           = Modifier.size(18.dp),
                )
            }
            Text(
                text  = "${w.main.temp.toInt()}°C",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
