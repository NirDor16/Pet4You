package com.example.pet4you.ui.dog

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
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
    val isEditMode        = !dogId.isNullOrEmpty()
    var name              by remember { mutableStateOf("") }
    var breed             by remember { mutableStateOf("") }
    var birthDate         by remember { mutableStateOf("") }
    var notes             by remember { mutableStateOf("") }
    var existingPhotoUrl  by remember { mutableStateOf("") }
    var selectedImageUri  by remember { mutableStateOf<Uri?>(null) }
    val dogActionState    by viewModel.dogActionState.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) selectedImageUri = uri }

    LaunchedEffect(dogId) { if (isEditMode) viewModel.loadDog(dogId!!) }

    LaunchedEffect(dogActionState) {
        if (isEditMode && dogActionState is DogActionState.DogLoaded) {
            val dog = (dogActionState as DogActionState.DogLoaded).dog
            name = dog.name; breed = dog.breed; birthDate = dog.birthDate
            notes = dog.notes; existingPhotoUrl = dog.photoUrl
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
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Photo picker circle
                Box(
                    modifier         = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    val imageModel = selectedImageUri ?: existingPhotoUrl.takeIf { it.isNotEmpty() }
                    if (imageModel != null) {
                        AsyncImage(
                            model              = imageModel,
                            contentDescription = "Dog photo",
                            modifier           = Modifier.fillMaxSize(),
                            contentScale       = ContentScale.Crop,
                        )
                    } else {
                        Icon(
                            imageVector        = Icons.Filled.CameraAlt,
                            contentDescription = "Add photo",
                            tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier           = Modifier.size(32.dp),
                        )
                    }
                }

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
                        if (isEditMode) viewModel.updateDog(dogId!!, name, breed, birthDate, notes, selectedImageUri)
                        else viewModel.addDog(name, breed, birthDate, notes, selectedImageUri)
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
