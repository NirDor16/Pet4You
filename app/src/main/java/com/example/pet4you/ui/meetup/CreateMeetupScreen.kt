package com.example.pet4you.ui.meetup

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pet4you.ui.components.Pet4YouTopBar
import com.example.pet4you.viewmodel.MeetupActionState
import com.example.pet4you.viewmodel.MeetupViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CreateMeetupScreen(
    onNavigateBack: () -> Unit,
    viewModel: MeetupViewModel = viewModel(),
) {
    val meetupActionState by viewModel.meetupActionState.collectAsState()

    var location          by remember { mutableStateOf("") }
    var description       by remember { mutableStateOf("") }
    var selectedDateTime  by remember { mutableStateOf(System.currentTimeMillis()) }
    var dogBreedsInput    by remember { mutableStateOf("") }
    var dogBreedsList     by remember { mutableStateOf<List<String>>(emptyList()) }

    val context       = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    LaunchedEffect(meetupActionState) {
        if (meetupActionState is MeetupActionState.Success) {
            viewModel.resetActionState(); onNavigateBack()
        }
    }

    val isLoading    = meetupActionState is MeetupActionState.Loading
    val errorMessage = (meetupActionState as? MeetupActionState.Error)?.message

    Scaffold(
        topBar = { Pet4YouTopBar(title = "Create Meetup", onBack = onNavigateBack) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            OutlinedTextField(
                value         = location,
                onValueChange = { location = it },
                label         = { Text("Location") },
                leadingIcon   = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
            )

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

            OutlinedTextField(
                value         = description,
                onValueChange = { description = it },
                label         = { Text("Description") },
                leadingIcon   = { Icon(Icons.Filled.Notes, contentDescription = null) },
                minLines      = 2,
                modifier      = Modifier.fillMaxWidth(),
            )

            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value         = dogBreedsInput,
                    onValueChange = { dogBreedsInput = it },
                    label         = { Text("Add dog breed") },
                    leadingIcon   = { Icon(Icons.Filled.Pets, contentDescription = null) },
                    singleLine    = true,
                    modifier      = Modifier.weight(1f),
                )
                Button(
                    onClick  = {
                        val breed = dogBreedsInput.trim()
                        if (breed.isNotEmpty() && !dogBreedsList.contains(breed)) {
                            dogBreedsList = dogBreedsList + breed; dogBreedsInput = ""
                        }
                    },
                    enabled = dogBreedsInput.isNotBlank(),
                ) { Text("Add") }
            }

            if (dogBreedsList.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dogBreedsList) { breed ->
                        InputChip(
                            selected      = false,
                            onClick       = { dogBreedsList = dogBreedsList - breed },
                            label         = { Text(breed) },
                            trailingIcon  = {
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
                onClick  = { viewModel.createMeetup(location, selectedDateTime, description, dogBreedsList) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled  = !isLoading && location.isNotBlank(),
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
    }
}
