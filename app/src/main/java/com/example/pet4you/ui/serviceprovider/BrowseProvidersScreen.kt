package com.example.pet4you.ui.serviceprovider

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.pet4you.data.model.ServiceProvider
import com.example.pet4you.viewmodel.BrowseProvidersState
import com.example.pet4you.viewmodel.BrowseProvidersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseProvidersScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: BrowseProvidersViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadProviders() }

    val activeFilter = (state as? BrowseProvidersState.Success)?.activeFilter

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Find Services") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter chips
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = activeFilter == null,
                        onClick = { viewModel.loadProviders(null) },
                        label = { Text("All") }
                    )
                }
                items(ProviderType.all) { type ->
                    FilterChip(
                        selected = activeFilter == type,
                        onClick = { viewModel.loadProviders(type) },
                        label = { Text(ProviderType.displayName(type)) }
                    )
                }
            }

            when (val s = state) {
                is BrowseProvidersState.Loading, BrowseProvidersState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is BrowseProvidersState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(s.message, color = Color.Red)
                    }
                }
                is BrowseProvidersState.Success -> {
                    if (s.providers.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No providers found.")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(s.providers) { provider ->
                                ProviderCard(
                                    provider = provider,
                                    onClick = { onNavigateToDetail(provider.serviceProviderId) }
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
private fun ProviderCard(
    provider: ServiceProvider,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = provider.fullName.ifEmpty { "Unnamed Provider" },
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = ProviderType.displayName(provider.providerType),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (provider.location.isNotEmpty()) {
                    Text(
                        text = provider.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Surface(
                color = if (provider.isAvailable)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = if (provider.isAvailable) "Available" else "Unavailable",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
