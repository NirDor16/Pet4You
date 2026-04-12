package com.example.pet4you.ui.dog

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pet4you.viewmodel.DogActionState
import com.example.pet4you.viewmodel.DogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDogScreen(
    dogId: String?,
    onNavigateBack: () -> Unit,
    viewModel: DogViewModel = viewModel()
) {
    val isEditMode = !dogId.isNullOrEmpty()

    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val dogActionState by viewModel.dogActionState.collectAsState()

    LaunchedEffect(dogId) {
        if (isEditMode) {
            viewModel.loadDog(dogId!!)
        }
    }

    LaunchedEffect(dogActionState) {
        if (isEditMode && dogActionState is DogActionState.DogLoaded) {
            val dog = (dogActionState as DogActionState.DogLoaded).dog
            name = dog.name
            breed = dog.breed
            birthDate = dog.birthDate
            notes = dog.notes
        }
        if (dogActionState is DogActionState.Success) {
            viewModel.resetActionState()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Dog" else "Add Dog") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isEditMode && dogActionState is DogActionState.Loading && name.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Breed") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = { Text("Birth Date (yyyy-MM-dd)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (dogActionState is DogActionState.Error) {
                    Text(
                        text = (dogActionState as DogActionState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (isEditMode) {
                            viewModel.updateDog(dogId!!, name, breed, birthDate, notes)
                        } else {
                            viewModel.addDog(name, breed, birthDate, notes)
                        }
                    },
                    enabled = dogActionState !is DogActionState.Loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (dogActionState is DogActionState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (isEditMode) "Save Changes" else "Add Dog")
                    }
                }
            }
        }
    }
}
