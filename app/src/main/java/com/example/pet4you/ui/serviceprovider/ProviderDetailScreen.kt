package com.example.pet4you.ui.serviceprovider

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pet4you.data.model.Dog
import com.example.pet4you.data.model.ProviderType
import com.example.pet4you.data.model.ServiceProvider
import com.example.pet4you.ui.components.ErrorMessage
import com.example.pet4you.ui.components.InfoRow
import com.example.pet4you.ui.components.LoadingBox
import com.example.pet4you.ui.components.PawBackground
import com.example.pet4you.ui.components.Pet4YouTopBar
import com.example.pet4you.viewmodel.ProviderDetailState
import com.example.pet4you.viewmodel.ProviderDetailViewModel
import com.example.pet4you.viewmodel.SendRequestState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDetailScreen(
    providerId: String,
    onNavigateBack: () -> Unit,
    viewModel: ProviderDetailViewModel = viewModel(),
) {
    val detailState      by viewModel.detailState.collectAsState()
    val sendRequestState by viewModel.sendRequestState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(providerId) { viewModel.load(providerId) }
    LaunchedEffect(sendRequestState) {
        if (sendRequestState is SendRequestState.Success) { showDialog = false; viewModel.resetSendState() }
    }

    Scaffold(
        topBar = { Pet4YouTopBar(title = "Provider Profile", onBack = onNavigateBack) },
    ) { padding ->
        when (val s = detailState) {
            is ProviderDetailState.Loading, ProviderDetailState.Idle -> LoadingBox(modifier = Modifier.padding(padding))
            is ProviderDetailState.Error -> ErrorMessage(s.message, modifier = Modifier.padding(padding))
            is ProviderDetailState.Loaded -> {
                ProviderDetailContent(
                    provider          = s.provider,
                    dogs              = s.dogs,
                    padding           = padding,
                    onSendRequestClick = { showDialog = true },
                )
                if (showDialog) {
                    SendRequestDialog(
                        provider         = s.provider,
                        dogs             = s.dogs,
                        sendRequestState = sendRequestState,
                        onSend           = { dogId, message ->
                            viewModel.sendRequest(s.provider.serviceProviderId, dogId, s.provider.providerType, message)
                        },
                        onDismiss = { showDialog = false; viewModel.resetSendState() },
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
    onSendRequestClick: () -> Unit,
) {
    PawBackground(modifier = Modifier.fillMaxSize().padding(padding)) {
    Column(
        modifier            = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ElevatedCard(
            modifier  = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text     = provider.fullName.ifEmpty { "Unnamed Provider" },
                        style    = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f),
                    )
                    Surface(
                        color        = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        shape        = MaterialTheme.shapes.extraSmall,
                    ) {
                        Text(
                            text     = ProviderType.displayName(provider.providerType),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style    = MaterialTheme.typography.labelMedium,
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                InfoRow(icon = Icons.Filled.Email, text = provider.email)
                if (provider.location.isNotEmpty()) {
                    InfoRow(icon = Icons.Filled.LocationOn, text = provider.location)
                }
                InfoRow(
                    icon = Icons.Filled.CheckCircle,
                    text = if (provider.isAvailable) "Available for new clients" else "Not currently available",
                    tint = if (provider.isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
            }
        }

        if (provider.description.isNotEmpty()) {
            ElevatedCard(
                modifier  = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoRow(icon = Icons.Filled.Notes, text = "About")
                    Text(
                        text  = provider.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        Button(
            onClick  = onSendRequestClick,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled  = dogs.isNotEmpty(),
        ) {
            Text(
                text  = if (dogs.isEmpty()) "No dogs registered" else "Send Service Request",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }       // closes Column(scroll+padding)
    }       // closes PawBackground
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SendRequestDialog(
    provider: ServiceProvider,
    dogs: List<Dog>,
    sendRequestState: SendRequestState,
    onSend: (dogId: String, message: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedDog        by remember { mutableStateOf<Dog?>(null) }
    var message            by remember { mutableStateOf("") }
    var dogDropdownExpanded by remember { mutableStateOf(false) }

    val isLoading    = sendRequestState is SendRequestState.Loading
    val errorMessage = (sendRequestState as? SendRequestState.Error)?.message

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Send Request to ${provider.fullName.ifEmpty { "Provider" }}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded         = dogDropdownExpanded,
                    onExpandedChange = { dogDropdownExpanded = it },
                ) {
                    OutlinedTextField(
                        value        = selectedDog?.name ?: "",
                        onValueChange = {},
                        readOnly     = true,
                        label        = { Text("Select Dog") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dogDropdownExpanded) },
                        modifier     = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded         = dogDropdownExpanded,
                        onDismissRequest = { dogDropdownExpanded = false },
                    ) {
                        dogs.forEach { dog ->
                            DropdownMenuItem(
                                text    = { Text("${dog.name} (${dog.breed})") },
                                onClick = { selectedDog = dog; dogDropdownExpanded = false },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value         = message,
                    onValueChange = { message = it },
                    label         = { Text("Message (optional)") },
                    modifier      = Modifier.fillMaxWidth(),
                    minLines      = 2,
                )

                if (errorMessage != null) {
                    Text(
                        text  = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedDog?.let { onSend(it.dogId, message) } },
                enabled = !isLoading && selectedDog != null,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Send")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Cancel") }
        },
    )
}
