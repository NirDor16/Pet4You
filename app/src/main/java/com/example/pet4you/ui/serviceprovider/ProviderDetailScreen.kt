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
import com.example.pet4you.data.model.Dog
import com.example.pet4you.data.model.ProviderType
import com.example.pet4you.data.model.ServiceProvider
import com.example.pet4you.viewmodel.ProviderDetailState
import com.example.pet4you.viewmodel.ProviderDetailViewModel
import com.example.pet4you.viewmodel.SendRequestState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDetailScreen(
    providerId: String,
    onNavigateBack: () -> Unit,
    viewModel: ProviderDetailViewModel = viewModel()
) {
    val detailState by viewModel.detailState.collectAsState()
    val sendRequestState by viewModel.sendRequestState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(providerId) { viewModel.load(providerId) }

    LaunchedEffect(sendRequestState) {
        if (sendRequestState is SendRequestState.Success) {
            showDialog = false
            viewModel.resetSendState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Provider Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val s = detailState) {
            is ProviderDetailState.Loading, ProviderDetailState.Idle -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            is ProviderDetailState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { Text(s.message, color = Color.Red) }
            }
            is ProviderDetailState.Loaded -> {
                ProviderDetailContent(
                    provider = s.provider,
                    dogs = s.dogs,
                    padding = padding,
                    onSendRequestClick = { showDialog = true }
                )
                if (showDialog) {
                    SendRequestDialog(
                        provider = s.provider,
                        dogs = s.dogs,
                        sendRequestState = sendRequestState,
                        onSend = { dogId, message ->
                            viewModel.sendRequest(s.provider.serviceProviderId, dogId, s.provider.providerType, message)
                        },
                        onDismiss = {
                            showDialog = false
                            viewModel.resetSendState()
                        }
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun ProviderDetailContent(
    provider: ServiceProvider,
    dogs: List<Dog>,
    padding: PaddingValues,
    onSendRequestClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = provider.fullName.ifEmpty { "Unnamed Provider" },
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = ProviderType.displayName(provider.providerType),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Email: ${provider.email}",
            style = MaterialTheme.typography.bodyMedium
        )
        if (provider.location.isNotEmpty()) {
            Text(
                text = "Location: ${provider.location}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = if (provider.isAvailable) "Available for new clients" else "Not currently available",
            style = MaterialTheme.typography.bodyMedium,
            color = if (provider.isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        if (provider.description.isNotEmpty()) {
            Divider()
            Text(
                text = provider.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSendRequestClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = dogs.isNotEmpty()
        ) {
            Text(if (dogs.isEmpty()) "No dogs registered" else "Send Request")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SendRequestDialog(
    provider: ServiceProvider,
    dogs: List<Dog>,
    sendRequestState: SendRequestState,
    onSend: (dogId: String, message: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDog by remember { mutableStateOf<Dog?>(null) }
    var message by remember { mutableStateOf("") }
    var dogDropdownExpanded by remember { mutableStateOf(false) }

    val isLoading = sendRequestState is SendRequestState.Loading
    val errorMessage = (sendRequestState as? SendRequestState.Error)?.message

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Send Request") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Dog selector
                ExposedDropdownMenuBox(
                    expanded = dogDropdownExpanded,
                    onExpandedChange = { dogDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedDog?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Dog") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dogDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dogDropdownExpanded,
                        onDismissRequest = { dogDropdownExpanded = false }
                    ) {
                        dogs.forEach { dog ->
                            DropdownMenuItem(
                                text = { Text("${dog.name} (${dog.breed})") },
                                onClick = {
                                    selectedDog = dog
                                    dogDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                if (errorMessage != null) {
                    Text(errorMessage, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedDog?.let { onSend(it.dogId, message) } },
                enabled = !isLoading && selectedDog != null
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp))
                else Text("Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel")
            }
        }
    )
}
