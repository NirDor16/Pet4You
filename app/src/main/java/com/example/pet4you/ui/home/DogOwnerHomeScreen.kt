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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.pet4you.ui.theme.DeepAmber
import com.example.pet4you.ui.theme.DeepBlue
import com.example.pet4you.ui.theme.DeepGreen
import com.example.pet4you.ui.theme.DeepOrange
import com.example.pet4you.ui.theme.DeepPurple
import com.example.pet4you.ui.theme.SoftBeige
import com.example.pet4you.ui.theme.SoftBlue
import com.example.pet4you.ui.theme.SoftGreen
import com.example.pet4you.ui.theme.SoftOrange
import com.example.pet4you.ui.theme.SoftPurple

private data class HomeItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val containerColor: Color,
    val iconTint: Color,
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
        HomeItem(Icons.Filled.Pets,         "My Dogs",       "Manage your dog profiles",      SoftBlue,   DeepBlue,   onNavigateToDogs),
        HomeItem(Icons.Filled.CalendarToday,"Reminders",     "Vaccines, grooming & more",     SoftOrange, DeepOrange, onNavigateToReminders),
        HomeItem(Icons.Filled.Group,        "Meetups",       "Find & join local meetups",      SoftGreen,  DeepGreen,  onNavigateToMeetups),
        HomeItem(Icons.Filled.Search,       "Find Services", "Vets, sitters & groomers",       SoftBeige,  DeepAmber,  onNavigateToProviders),
        HomeItem(Icons.Filled.SmartToy,     "AI Chat",       "Get pet care advice instantly",  SoftPurple, DeepPurple, onNavigateToAiChat),
    )

    Column(modifier = Modifier.fillMaxSize()) {

            // ── Hero header ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                MaterialTheme.colorScheme.primaryContainer,
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
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
                    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("lottie_dog.json"))
                    val progress    by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
                    if (composition != null) {
                        LottieAnimation(
                            composition = composition,
                            progress    = { progress },
                            modifier    = Modifier.size(100.dp),
                        )
                    } else {
                        Icon(
                            imageVector        = Icons.Filled.Pets,
                            contentDescription = null,
                            modifier           = Modifier.size(60.dp),
                            tint               = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector        = Icons.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint               = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }

            // ── Feature cards ─────────────────────────────────────────────────
            LazyColumn(
                contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
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
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        colors    = CardDefaults.elevatedCardColors(
            containerColor = Color.White,
        ),
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier         = Modifier
                    .size(52.dp)
                    .background(
                        color = item.containerColor,
                        shape = MaterialTheme.shapes.medium,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = item.icon,
                    contentDescription = null,
                    tint               = item.iconTint,
                    modifier           = Modifier.size(28.dp),
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text  = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
