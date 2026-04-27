package com.example.pet4you.ui.serviceprovider

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pet4you.data.model.ProviderType
import com.example.pet4you.viewmodel.ProfileActionState
import com.example.pet4you.viewmodel.ProfileState
import com.example.pet4you.viewmodel.ServiceProviderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceProviderProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ServiceProviderViewModel = viewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    val profileActionState by viewModel.profileActionState.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isAvailable by remember { mutableStateOf(true) }

    var email by remember { mutableStateOf("") }
    var providerType by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadProfile() }

    LaunchedEffect(profileState) {
        if (profileState is ProfileState.Loaded) {
            val provider = (profileState as ProfileState.Loaded).provider
            fullName = provider.fullName
            description = provider.description
            location = provider.location
            isAvailable = provider.isAvailable
            email = provider.email
            providerType = provider.providerType
        }
    }

    LaunchedEffect(profileActionState) {
        if (profileActionState is ProfileActionState.Success) {
            viewModel.resetActionState()
            onNavigateBack()
        }
    }

    val isLoading = profileActionState is ProfileActionState.Loading
    val errorMessage = (profileActionState as? ProfileActionState.Error)?.message

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (profileState) {
            is ProfileState.Loading, ProfileState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ProfileState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (profileState as ProfileState.Error).message,
                        color = Color.Red
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Read-only info
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Provider Type: ${ProviderType.displayName(providerType)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Email: $email",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Available for new clients", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = isAvailable,
                            onCheckedChange = { isAvailable = it }
                        )
                    }

                    if (errorMessage != null) {
                        Text(errorMessage, color = Color.Red)
                    }

                    Button(
                        onClick = {
                            viewModel.updateProfile(fullName, description, location, isAvailable)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && fullName.isNotBlank()
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        else Text("Save")
                    }
                }
            }
        }
    }
}
