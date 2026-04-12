package com.example.pet4you.ui.dog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pet4you.data.model.Dog
import com.example.pet4you.viewmodel.DogActionState
import com.example.pet4you.viewmodel.DogListState
import com.example.pet4you.viewmodel.DogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DogListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (dogId: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: DogViewModel = viewModel()
) {
    val dogListState by viewModel.dogListState.collectAsState()
    val dogActionState by viewModel.dogActionState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDogs()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Dogs") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add Dog")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (dogActionState is DogActionState.Error) {
                Text(
                    text = (dogActionState as DogActionState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            when (dogListState) {
                is DogListState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is DogListState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = (dogListState as DogListState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is DogListState.Success -> {
                    val dogs = (dogListState as DogListState.Success).dogs
                    if (dogs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No dogs yet. Tap + to add your first dog!",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(dogs) { dog ->
                                DogCard(
                                    dog = dog,
                                    onEdit = { onNavigateToEdit(dog.dogId) },
                                    onDelete = { viewModel.deleteDog(dog.dogId) }
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
private fun DogCard(
    dog: Dog,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(dog.name, style = MaterialTheme.typography.titleMedium)
                Text(dog.breed, style = MaterialTheme.typography.bodyMedium)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}
