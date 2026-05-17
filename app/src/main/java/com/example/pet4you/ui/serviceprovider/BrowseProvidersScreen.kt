package com.example.pet4you.ui.serviceprovider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pet4you.data.model.ProviderType
import com.example.pet4you.data.model.ServiceProvider
import com.example.pet4you.ui.components.EmptyState
import com.example.pet4you.ui.components.ErrorMessage
import com.example.pet4you.ui.components.LoadingBox
import com.example.pet4you.ui.components.Pet4YouTopBar
import com.example.pet4you.viewmodel.BrowseProvidersState
import com.example.pet4you.viewmodel.BrowseProvidersViewModel

@Composable
fun BrowseProvidersScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: BrowseProvidersViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadProviders() }

    val activeFilter = (state as? BrowseProvidersState.Success)?.activeFilter

    Scaffold(
        topBar = { Pet4YouTopBar(title = "Find Services", onBack = onNavigateBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyRow(
                modifier              = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FilterChip(
                        selected = activeFilter == null,
                        onClick  = { viewModel.loadProviders(null) },
                        label    = { Text("All") },
                    )
                }
                items(ProviderType.all) { type ->
                    FilterChip(
                        selected = activeFilter == type,
                        onClick  = { viewModel.loadProviders(type) },
                        label    = { Text(ProviderType.displayName(type)) },
                    )
                }
            }

            when (val s = state) {
                is BrowseProvidersState.Loading, BrowseProvidersState.Idle -> LoadingBox()
                is BrowseProvidersState.Error -> ErrorMessage(s.message)
                is BrowseProvidersState.Success -> {
                    if (s.providers.isEmpty()) {
                        EmptyState(
                            icon     = Icons.Filled.Search,
                            title    = "No providers found",
                            subtitle = "Try a different filter",
                        )
                    } else {
                        LazyColumn(
                            contentPadding      = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(s.providers, key = { it.serviceProviderId }) { provider ->
                                ProviderCard(provider = provider, onClick = { onNavigateToDetail(provider.serviceProviderId) })
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
private fun ProviderCard(provider: ServiceProvider, onClick: () -> Unit) {
    ElevatedCard(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier         = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text  = provider.fullName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = provider.fullName.ifEmpty { "Unnamed Provider" },
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text  = ProviderType.displayName(provider.providerType),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (provider.location.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier           = Modifier.size(12.dp),
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text  = provider.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Surface(
                color        = if (provider.isAvailable) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (provider.isAvailable) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                shape        = MaterialTheme.shapes.extraSmall,
            ) {
                Text(
                    text     = if (provider.isAvailable) "Available" else "Unavailable",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style    = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
