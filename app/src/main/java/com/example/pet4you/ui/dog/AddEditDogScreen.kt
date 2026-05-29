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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.produceState
import com.example.pet4you.ui.components.SuccessOverlay
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.pet4you.data.model.DogAvatar
import com.example.pet4you.repository.DogCeoRepository
import com.example.pet4you.ui.components.BreedSelector
import com.example.pet4you.ui.components.DogAvatarCanvas
import com.example.pet4you.ui.components.LoadingBox
import com.example.pet4you.ui.components.Pet4YouTopBar
import com.example.pet4you.viewmodel.DogActionState
import com.example.pet4you.viewmodel.DogViewModel
import com.google.gson.Gson

@Composable
fun AddEditDogScreen(
    dogId: String?,
    onNavigateBack: () -> Unit,
    onNavigateToAvatar: (breed: String, avatarJson: String?) -> Unit = { _, _ -> },
    resultAvatarJson: String? = null,
    viewModel: DogViewModel = viewModel(),
) {
    val isEditMode        = !dogId.isNullOrEmpty()
    var name              by remember { mutableStateOf("") }
    var breed             by remember { mutableStateOf("") }
    var birthDate         by remember { mutableStateOf("") }
    var notes             by remember { mutableStateOf("") }
    var existingPhotoUrl  by remember { mutableStateOf("") }
    var selectedImageUri  by remember { mutableStateOf<Uri?>(null) }
    var pendingAvatar     by remember { mutableStateOf<DogAvatar?>(null) }
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
            pendingAvatar = dog.avatar
        }
        if (dogActionState is DogActionState.Success) {
            delay(1400)
            viewModel.resetActionState()
            onNavigateBack()
        }
    }

    LaunchedEffect(resultAvatarJson) {
        if (!resultAvatarJson.isNullOrEmpty()) {
            runCatching { Gson().fromJson(resultAvatarJson, DogAvatar::class.java) }
                .getOrNull()?.let { pendingAvatar = it }
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
        if (dogActionState is DogActionState.Success) {
            SuccessOverlay(message = if (isEditMode) "Changes saved!" else "Dog added!")
        } else if (isEditMode && isLoading) {
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

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick  = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Upload Photo")
                    }
                    OutlinedButton(
                        onClick  = {
                            val currentJson = pendingAvatar?.let { Gson().toJson(it) }
                            onNavigateToAvatar(breed, currentJson)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Pets, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (pendingAvatar != null) "Edit Avatar" else "Create Avatar")
                    }
                }

                if (pendingAvatar != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        DogAvatarCanvas(
                            avatar   = pendingAvatar!!,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Avatar preview",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

                if (breed.isNotBlank() && selectedImageUri == null && existingPhotoUrl.isEmpty()) {
                    BreedInspirationCard(breed = breed)
                }

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
                        if (isEditMode) viewModel.updateDog(dogId!!, name, breed, birthDate, notes, selectedImageUri, avatar = pendingAvatar)
                        else viewModel.addDog(name, breed, birthDate, notes, selectedImageUri, avatar = pendingAvatar)
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

@Composable
private fun BreedInspirationCard(breed: String) {
    var refreshKey by remember { mutableIntStateOf(0) }

    val imageUrl by produceState<String?>(null, breed, refreshKey) {
        value = null
        value = DogCeoRepository.getBreedImageUrl(breed)
    }

    Surface(
        shape  = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            ) {
                if (imageUrl != null) {
                    SubcomposeAsyncImage(
                        model              = imageUrl,
                        contentDescription = breed,
                        modifier           = Modifier.fillMaxSize(),
                        contentScale       = ContentScale.Crop,
                        loading            = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    modifier   = Modifier.size(32.dp),
                                    strokeWidth = 2.dp,
                                    color      = MaterialTheme.colorScheme.primary,
                                )
                            }
                        },
                    )
                } else {
                    Box(
                        modifier         = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(32.dp),
                            strokeWidth = 2.dp,
                            color       = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)),
                                startY = 80f,
                            )
                        ),
                )

                Text(
                    text     = breed,
                    style    = MaterialTheme.typography.titleSmall,
                    color    = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                )
            }

            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Filled.Pets,
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp),
                        tint               = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text  = "Breed inspiration",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                FilledTonalButton(
                    onClick      = {
                        DogCeoRepository.clearBreedCache(breed)
                        refreshKey++
                    },
                    modifier     = Modifier.height(32.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New photo", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
