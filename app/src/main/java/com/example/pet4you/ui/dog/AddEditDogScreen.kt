package com.example.pet4you.ui.dog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pet4you.ui.components.BreedSelector
import com.example.pet4you.ui.components.LoadingBox
import com.example.pet4you.ui.components.Pet4YouTopBar
import com.example.pet4you.viewmodel.DogActionState
import com.example.pet4you.viewmodel.DogViewModel

@Composable
fun AddEditDogScreen(
    dogId: String?,
    onNavigateBack: () -> Unit,
    viewModel: DogViewModel = viewModel(),
) {
    val isEditMode     = !dogId.isNullOrEmpty()
    var name           by remember { mutableStateOf("") }
    var breed          by remember { mutableStateOf("") }
    var birthDate      by remember { mutableStateOf("") }
    var notes          by remember { mutableStateOf("") }
    val dogActionState by viewModel.dogActionState.collectAsState()

    LaunchedEffect(dogId) { if (isEditMode) viewModel.loadDog(dogId!!) }

    LaunchedEffect(dogActionState) {
        if (isEditMode && dogActionState is DogActionState.DogLoaded) {
            val dog = (dogActionState as DogActionState.DogLoaded).dog
            name = dog.name; breed = dog.breed; birthDate = dog.birthDate; notes = dog.notes
        }
        if (dogActionState is DogActionState.Success) {
            viewModel.resetActionState(); onNavigateBack()
        }
    }

    val isLoading = dogActionState is DogActionState.Loading && name.isEmpty()

    Scaffold(
        topBar = {
            Pet4YouTopBar(
                title  = if (isEditMode) "Edit Dog" else "Add Dog",
                onBack = onNavigateBack,
            )
        },
    ) { paddingValues ->
        if (isEditMode && isLoading) {
            LoadingBox()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = { Text("Name") },
                    leadingIcon   = { Icon(Icons.Filled.Pets, contentDescription = null) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                )
                BreedSelector(
                    value         = breed,
                    onValueChange = { breed = it },
                )
                OutlinedTextField(
                    value         = birthDate,
                    onValueChange = { birthDate = it },
                    label         = { Text("Birth Date (yyyy-MM-dd)") },
                    leadingIcon   = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value         = notes,
                    onValueChange = { notes = it },
                    label         = { Text("Notes") },
                    leadingIcon   = { Icon(Icons.Filled.Notes, contentDescription = null) },
                    minLines      = 2,
                    modifier      = Modifier.fillMaxWidth(),
                )

                if (dogActionState is DogActionState.Error) {
                    Text(
                        text  = (dogActionState as DogActionState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Spacer(Modifier.height(4.dp))

                Button(
                    onClick  = {
                        if (isEditMode) viewModel.updateDog(dogId!!, name, breed, birthDate, notes)
                        else viewModel.addDog(name, breed, birthDate, notes)
                    },
                    enabled  = dogActionState !is DogActionState.Loading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    if (dogActionState is DogActionState.Loading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color       = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(
                            text  = if (isEditMode) "Save Changes" else "Add Dog",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}
