package com.example.pet4you.ui.meetup

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pet4you.data.model.Meetup
import com.example.pet4you.network.WeatherResponse
import com.example.pet4you.network.DogParkResult
import com.example.pet4you.repository.WeatherRepository
import com.example.pet4you.ui.components.ErrorMessage
import com.example.pet4you.ui.components.InfoRow
import com.example.pet4you.ui.components.LoadingBox
import com.example.pet4you.ui.components.Pet4YouTopBar
import com.example.pet4you.viewmodel.MeetupActionState
import com.example.pet4you.viewmodel.MeetupDetailState
import com.example.pet4you.viewmodel.MeetupViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MeetupDetailScreen(
    meetupId: String,
    onNavigateBack: () -> Unit,
    viewModel: MeetupViewModel = viewModel(),
) {
    val detailState by viewModel.detailState.collectAsState()
    val actionState by viewModel.meetupActionState.collectAsState()
    var isDeleting  by remember { mutableStateOf(false) }

    LaunchedEffect(meetupId) { viewModel.loadMeetupById(meetupId) }
    LaunchedEffect(actionState) {
        if (actionState is MeetupActionState.Success) {
            viewModel.resetActionState()
            if (isDeleting) {
                onNavigateBack()
            } else {
                viewModel.loadMeetupById(meetupId)
            }
        }
    }

    Scaffold(
        topBar = {
            val title = (detailState as? MeetupDetailState.Success)?.meetup?.title?.ifEmpty { "Meetup" } ?: "Meetup"
            Pet4YouTopBar(title = title, onBack = onNavigateBack)
        },
    ) { padding ->
        when (val s = detailState) {
            is MeetupDetailState.Loading, MeetupDetailState.Idle -> LoadingBox(modifier = Modifier.padding(padding))
            is MeetupDetailState.Error -> ErrorMessage(s.message, modifier = Modifier.padding(padding))
            is MeetupDetailState.Success -> MeetupDetailContent(
                meetup        = s.meetup,
                currentUserId = s.currentUserId,
                actionState   = actionState,
                padding       = padding,
                onJoin        = { viewModel.joinMeetupFromDetail(meetupId) },
                onLeave       = { viewModel.leaveMeetupFromDetail(meetupId) },
                onDelete      = { isDeleting = true; viewModel.deleteMeetupFromDetail(meetupId) },
            )
            else -> {}
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MeetupDetailContent(
    meetup: Meetup,
    currentUserId: String?,
    actionState: MeetupActionState,
    padding: PaddingValues,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    val dateStr = remember(meetup.dateTime) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(meetup.dateTime))
    }
    val isCreator     = meetup.creatorId == currentUserId
    val isParticipant = currentUserId != null && currentUserId in meetup.participants
    val isFull        = meetup.participantLimit > 0 && meetup.participants.size >= meetup.participantLimit
    val isLoading     = actionState is MeetupActionState.Loading
    val errorMessage  = (actionState as? MeetupActionState.Error)?.message

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ElevatedCard(
            modifier  = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(meetup.title.ifEmpty { "Meetup" }, style = MaterialTheme.typography.headlineSmall)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InfoRow(
                        icon     = Icons.Filled.LocationOn,
                        text     = meetup.location,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        onClick = {
                            val uri = Uri.parse("google.navigation:q=${Uri.encode(meetup.location)}")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            } else {
                                val fallback = Uri.parse("https://maps.google.com/maps?q=${Uri.encode(meetup.location)}")
                                context.startActivity(Intent(Intent.ACTION_VIEW, fallback))
                            }
                        }
                    ) {
                        Text(
                            text  = "Navigate →",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                InfoRow(icon = Icons.Filled.CalendarToday, text = dateStr)
                val participantText = if (meetup.participantLimit > 0)
                    "${meetup.participants.size} / ${meetup.participantLimit} participants"
                else
                    "${meetup.participants.size} participant(s)"
                InfoRow(icon = Icons.Filled.Group, text = participantText)
            }
        }

        WeatherCard(location = meetup.location)

        if (meetup.description.isNotEmpty()) {
            ElevatedCard(
                modifier  = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoRow(icon = Icons.Filled.Notes, text = "Description")
                    Text(
                        text  = meetup.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (meetup.dogBreeds.isNotEmpty()) {
            ElevatedCard(
                modifier  = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoRow(icon = Icons.Filled.Pets, text = "Dog Breeds")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        meetup.dogBreeds.forEach { breed ->
                            AssistChip(onClick = {}, label = { Text(breed) })
                        }
                    }
                }
            }
        }

        if (errorMessage != null) {
            Text(
                text  = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(Modifier.height(4.dp))

        when {
            isCreator -> {
                Text(
                    text  = "Your meetup",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick  = onDelete,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled  = !isLoading,
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text("Delete Meetup", style = MaterialTheme.typography.labelLarge)
                }
            }
            isParticipant -> {
                OutlinedButton(
                    onClick  = onLeave,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled  = !isLoading,
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text("Leave Meetup", style = MaterialTheme.typography.labelLarge)
                }
            }
            else -> {
                Button(
                    onClick  = onJoin,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled  = !isLoading && !isFull,
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    else Text(if (isFull) "Meetup Full" else "Join Meetup", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun WeatherCard(location: String) {
    val weather by produceState<WeatherResponse?>(null, location) {
        value = WeatherRepository.getWeather(location)
    }

    weather?.let { w ->
        val condition   = w.weather.firstOrNull()
        val iconUrl     = condition?.icon?.let { "https://openweathermap.org/img/wn/$it@2x.png" }
        val description = condition?.description?.replaceFirstChar { it.uppercase() } ?: ""
        val tempInt     = w.main.temp.toInt()

        ElevatedCard(
            modifier  = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow(icon = Icons.Filled.WbSunny, text = "Weather at location")
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (iconUrl != null) {
                        AsyncImage(
                            model              = iconUrl,
                            contentDescription = description,
                            modifier           = Modifier.size(52.dp),
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector        = Icons.Filled.Thermostat,
                                contentDescription = null,
                                modifier           = Modifier.size(18.dp),
                                tint               = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text  = "$tempInt°C  •  $description",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector        = Icons.Filled.WaterDrop,
                                    contentDescription = null,
                                    modifier           = Modifier.size(14.dp),
                                    tint               = MaterialTheme.colorScheme.secondary,
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text  = "${w.main.humidity}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector        = Icons.Filled.Air,
                                    contentDescription = null,
                                    modifier           = Modifier.size(14.dp),
                                    tint               = MaterialTheme.colorScheme.secondary,
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text  = "${"%.1f".format(w.wind.speed)} m/s",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
