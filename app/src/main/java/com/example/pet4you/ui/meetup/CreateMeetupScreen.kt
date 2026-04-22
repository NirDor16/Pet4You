package com.example.pet4you.ui.meetup

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pet4you.viewmodel.MeetupActionState
import com.example.pet4you.viewmodel.MeetupViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CreateMeetupScreen(
    onNavigateBack: () -> Unit,
    viewModel: MeetupViewModel = viewModel()
) {
    val meetupActionState by viewModel.meetupActionState.collectAsState()

    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDateTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var dogBreedsInput by remember { mutableStateOf("") }
    var dogBreedsList by remember { mutableStateOf<List<String>>(emptyList()) }

    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    LaunchedEffect(meetupActionState) {
        if (meetupActionState is MeetupActionState.Success) {
            viewModel.resetActionState()
            onNavigateBack()
        }
    }

    val isLoading = meetupActionState is MeetupActionState.Loading
    val errorMessage = (meetupActionState as? MeetupActionState.Error)?.message

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Create Meetup", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedButton(
            onClick = {
                val cal = Calendar.getInstance().apply { timeInMillis = selectedDateTime }
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                cal.set(year, month, day, hour, minute, 0)
                                selectedDateTime = cal.timeInMillis
                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Date & Time: ${dateFormatter.format(Date(selectedDateTime))}")
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        // Dog breeds input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = dogBreedsInput,
                onValueChange = { dogBreedsInput = it },
                label = { Text("Add dog breed") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                onClick = {
                    val breed = dogBreedsInput.trim()
                    if (breed.isNotEmpty() && !dogBreedsList.contains(breed)) {
                        dogBreedsList = dogBreedsList + breed
                        dogBreedsInput = ""
                    }
                },
                enabled = dogBreedsInput.isNotBlank()
            ) {
                Text("Add")
            }
        }

        if (dogBreedsList.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(dogBreedsList) { breed ->
                    InputChip(
                        selected = false,
                        onClick = { dogBreedsList = dogBreedsList - breed },
                        label = { Text(breed) },
                        trailingIcon = {
                            Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }
        }

        if (errorMessage != null) {
            Text(errorMessage, color = Color.Red)
        }

        Button(
            onClick = {
                viewModel.createMeetup(location, selectedDateTime, description, dogBreedsList)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && location.isNotBlank()
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp))
            else Text("Create Meetup")
        }

        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}
