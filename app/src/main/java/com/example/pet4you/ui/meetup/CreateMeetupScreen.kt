package com.example.pet4you.ui.meetup

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pet4you.network.DogParkResult
import com.example.pet4you.network.WeatherResponse
import com.example.pet4you.repository.DogParkRepository
import com.example.pet4you.repository.WeatherRepository
import com.example.pet4you.ui.components.BreedSelector
import com.example.pet4you.ui.components.PawBackground
import com.example.pet4you.ui.components.Pet4YouTopBar

import com.example.pet4you.ui.components.SuccessOverlay
import com.example.pet4you.ui.theme.DeepGreen
import com.example.pet4you.ui.theme.SoftGreen
import com.example.pet4you.viewmodel.MeetupActionState
import com.example.pet4you.viewmodel.MeetupViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMeetupScreen(
    onNavigateBack: () -> Unit,
    viewModel: MeetupViewModel = viewModel(),
) {
    val meetupActionState by viewModel.meetupActionState.collectAsState()

    var title            by remember { mutableStateOf("") }
    var location         by remember { mutableStateOf("") }
    var description      by remember { mutableStateOf("") }
    var selectedDateTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var participantLimit by remember { mutableStateOf("") }
    var dogBreedsInput   by remember { mutableStateOf("") }
    var dogBreedsList    by remember { mutableStateOf<List<String>>(emptyList()) }

    var showParkSheet    by remember { mutableStateOf(false) }
    var nearbyParks      by remember { mutableStateOf<List<DogParkResult>>(emptyList()) }
    var isLoadingParks   by remember { mutableStateOf(false) }
    var parkError        by remember { mutableStateOf<String?>(null) }

    val context          = LocalContext.current
    val scope            = rememberCoroutineScope()
    val dateFormatter    = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val locationClient   = remember { LocationServices.getFusedLocationProviderClient(context) }
    val sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    @SuppressLint("MissingPermission")
    fun fetchParks() {
        scope.launch {
            isLoadingParks = true
            parkError = null
            try {
                val cts      = CancellationTokenSource()
                val loc      = locationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token).await()
                if (loc == null) {
                    parkError = "Could not get location. Please enter manually."
                } else {
                    val parks = DogParkRepository.getNearbyDogParks(loc.latitude, loc.longitude)
                    if (parks.isEmpty()) parkError = "No dog parks found nearby."
                    else { nearbyParks = parks; showParkSheet = true }
                }
            } catch (e: Exception) {
                parkError = "Location error. Please enter manually."
            } finally {
                isLoadingParks = false
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) fetchParks()
        else parkError = "Location permission denied. Please enter manually."
    }

    LaunchedEffect(meetupActionState) {
        if (meetupActionState is MeetupActionState.Success) {
            delay(1400)
            viewModel.resetActionState()
            onNavigateBack()
        }
    }

    val isLoading    = meetupActionState is MeetupActionState.Loading
    val errorMessage = (meetupActionState as? MeetupActionState.Error)?.message

    if (meetupActionState is MeetupActionState.Success) {
        SuccessOverlay(message = "Meetup created!")
        return@CreateMeetupScreen
    }

    if (showParkSheet) {
        ModalBottomSheet(
            onDismissRequest = { showParkSheet = false },
            sheetState       = sheetState,
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    text     = "Nearby Dog Parks",
                    style    = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                )
                HorizontalDivider()
                LazyColumn {
                    items(nearbyParks) { park ->
                        ParkItem(
                            park    = park,
                            onClick = {
                                location = buildString {
                                    append(park.title)
                                    if (!park.address.isNullOrBlank()) append(", ${park.address}")
                                }
                                showParkSheet = false
                            },
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = { Pet4YouTopBar(title = "Create Meetup", onBack = onNavigateBack) },
    ) { paddingValues ->
        PawBackground(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier            = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
            OutlinedTextField(
                value         = title,
                onValueChange = { title = it },
                label         = { Text("Title") },
                leadingIcon   = { Icon(Icons.Filled.Edit, contentDescription = null) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value         = location,
                onValueChange = { location = it },
                label         = { Text("Location") },
                leadingIcon   = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
            )

            FilledTonalButton(
                onClick  = {
                    parkError = null
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) fetchParks()
                    else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled  = !isLoadingParks,
            ) {
                if (isLoadingParks) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Finding parks...")
                } else {
                    Icon(Icons.Filled.NearMe, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Find nearby dog parks")
                }
            }

            if (parkError != null) {
                Text(
                    text  = parkError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            OutlinedButton(
                onClick  = {
                    val cal = Calendar.getInstance().apply { timeInMillis = selectedDateTime }
                    DatePickerDialog(context, { _, year, month, day ->
                        TimePickerDialog(context, { _, hour, minute ->
                            cal.set(year, month, day, hour, minute, 0)
                            selectedDateTime = cal.timeInMillis
                        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                Text("  ${dateFormatter.format(Date(selectedDateTime))}")
            }

            if (location.isNotBlank()) {
                WeatherPreview(location = location, datetimeMs = selectedDateTime)
            }

            OutlinedTextField(
                value         = description,
                onValueChange = { description = it },
                label         = { Text("Description") },
                leadingIcon   = { Icon(Icons.Filled.Notes, contentDescription = null) },
                minLines      = 2,
                modifier      = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value           = participantLimit,
                onValueChange   = { if (it.all { c -> c.isDigit() }) participantLimit = it },
                label           = { Text("Participant Limit (0 = unlimited)") },
                leadingIcon     = { Icon(Icons.Filled.Pets, contentDescription = null) },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier        = Modifier.fillMaxWidth(),
            )

            BreedSelector(
                value         = dogBreedsInput,
                onValueChange = { dogBreedsInput = it },
                label         = "Add dog breed",
                onBreedSelected = { breed ->
                    if (breed.isNotBlank() && breed !in dogBreedsList) {
                        dogBreedsList = dogBreedsList + breed
                    }
                    dogBreedsInput = ""
                },
            )

            if (dogBreedsList.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dogBreedsList) { breed ->
                        InputChip(
                            selected     = false,
                            onClick      = { dogBreedsList = dogBreedsList - breed },
                            label        = { Text(breed) },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                            },
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick  = {
                    val limit = participantLimit.toIntOrNull() ?: 0
                    viewModel.createMeetup(title, location, selectedDateTime, description, dogBreedsList, limit)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled  = !isLoading && title.isNotBlank() && location.isNotBlank(),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Create Meetup", style = MaterialTheme.typography.labelLarge)
                }
            }

            OutlinedButton(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
        }   // closes PawBackground
    }
}

@Composable
private fun WeatherPreview(location: String, datetimeMs: Long) {
    val weather by produceState<WeatherResponse?>(null, location, datetimeMs) {
        value = WeatherRepository.getWeatherForDate(location, datetimeMs)
    }
    weather?.let { w ->
        val condition   = w.weather.firstOrNull()
        val iconUrl     = condition?.icon?.let { "https://openweathermap.org/img/wn/$it@2x.png" }
        val description = condition?.description?.replaceFirstChar { it.uppercase() } ?: ""
        ElevatedCard(
            modifier  = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        ) {
            Row(
                modifier          = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (iconUrl != null) {
                    coil.compose.AsyncImage(
                        model              = iconUrl,
                        contentDescription = description,
                        modifier           = Modifier.size(40.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text  = "Expected weather",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text  = "${w.main.temp.toInt()}°C  •  $description",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun ParkItem(park: DogParkResult, onClick: () -> Unit) {
    Button(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors   = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor   = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = androidx.compose.material3.ButtonDefaults.buttonElevation(0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Filled.LocationOn,
                    contentDescription = null,
                    modifier           = Modifier.size(18.dp),
                    tint               = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = park.title,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            if (!park.address.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text     = park.address,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 26.dp),
                )
            }
            if (park.rating != null) {
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.padding(start = 26.dp),
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Star,
                        contentDescription = null,
                        modifier           = Modifier.size(13.dp),
                        tint               = MaterialTheme.colorScheme.tertiary,
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text  = "${"%.1f".format(park.rating)}${if (park.reviews != null) "  (${park.reviews})" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
