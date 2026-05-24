package com.example.pet4you.ui.dog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pet4you.data.model.Dog
import com.example.pet4you.ui.components.EmptyState
import com.example.pet4you.ui.components.ErrorMessage
import com.example.pet4you.ui.components.LoadingBox
import com.example.pet4you.ui.components.Pet4YouTopBar
import com.example.pet4you.viewmodel.DogActionState
import com.example.pet4you.viewmodel.DogListState
import com.example.pet4you.viewmodel.DogViewModel

@Composable
fun DogListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (dogId: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: DogViewModel = viewModel(),
) {
    val dogListState   by viewModel.dogListState.collectAsState()
    val dogActionState by viewModel.dogActionState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadDogs() }

    Scaffold(
        topBar = { Pet4YouTopBar(title = "My Dogs", onBack = onNavigateBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick           = onNavigateToAdd,
                containerColor    = MaterialTheme.colorScheme.primary,
                contentColor      = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Dog")
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (dogActionState is DogActionState.Error) {
                ErrorMessage(
                    message  = (dogActionState as DogActionState.Error).message,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            when (dogListState) {
                is DogListState.Loading -> LoadingBox()
                is DogListState.Error   -> ErrorMessage((dogListState as DogListState.Error).message)
                is DogListState.Success -> {
                    val dogs = (dogListState as DogListState.Success).dogs
                    if (dogs.isEmpty()) {
                        EmptyState(
                            icon     = Icons.Filled.Pets,
                            title    = "No dogs yet",
                            subtitle = "Tap + to add your first dog",
                        )
                    } else {
                        LazyColumn(
                            contentPadding      = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(dogs, key = { it.dogId }) { dog ->
                                DogCard(
                                    dog      = dog,
                                    onEdit   = { onNavigateToEdit(dog.dogId) },
                                    onDelete = { viewModel.deleteDog(dog.dogId) },
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

@Composable
private fun DogCard(dog: Dog, onEdit: () -> Unit, onDelete: () -> Unit) {
    ElevatedCard(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (dog.photoUrl.isNotEmpty()) {
                AsyncImage(
                    model              = dog.photoUrl,
                    contentDescription = dog.name,
                    modifier           = Modifier.size(44.dp).clip(CircleShape),
                    contentScale       = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier         = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text  = dog.breed.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = dog.name,
                    style    = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text  = dog.breed,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
