package com.example.pet4you.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private data class HomeItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit,
)

@Composable
fun DogOwnerHomeScreen(
    onLogout: () -> Unit,
    onNavigateToDogs: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToMeetups: () -> Unit,
    onNavigateToProviders: () -> Unit,
    onNavigateToAiChat: () -> Unit,
) {
    val items = listOf(
        HomeItem(Icons.Filled.Pets,      "My Dogs",       "Manage your dog profiles",     onNavigateToDogs),
        HomeItem(Icons.Filled.CalendarToday, "Reminders", "Vaccines, grooming & more",    onNavigateToReminders),
        HomeItem(Icons.Filled.Group,     "Meetups",       "Find & join local meetups",     onNavigateToMeetups),
        HomeItem(Icons.Filled.Search,    "Find Services", "Vets, sitters & groomers",      onNavigateToProviders),
        HomeItem(Icons.Filled.SmartToy,  "AI Chat",       "Get pet care advice instantly", onNavigateToAiChat),
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Branded header ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer,
                        )
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            Column {
                Text(
                    text  = "Pet4You",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = "Welcome back!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                )
            }
            IconButton(
                onClick  = onLogout,
                modifier = Modifier.align(Alignment.TopEnd),
            ) {
                Icon(
                    imageVector        = Icons.Filled.ExitToApp,
                    contentDescription = "Logout",
                    tint               = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }

        // ── Nav cards ─────────────────────────────────────────────────────────
        LazyColumn(
            contentPadding      = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier            = Modifier.fillMaxSize(),
        ) {
            items(items) { item ->
                HomeNavCard(item)
            }
        }
    }
}

@Composable
private fun HomeNavCard(item: HomeItem) {
    ElevatedCard(
        onClick   = item.onClick,
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier         = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = item.icon,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier           = Modifier.size(26.dp),
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text  = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text  = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
