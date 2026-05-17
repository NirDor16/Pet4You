package com.example.pet4you.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.example.pet4you.repository.AuthRepository
import com.example.pet4you.ui.navigation.homeRouteForRole
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onDestinationReady: (route: String) -> Unit,
) {
    val repository = remember { AuthRepository() }
    val scope      = rememberCoroutineScope()

    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        launch { alpha.animateTo(1f, animationSpec = tween(600)) }
        launch { scale.animateTo(1f, animationSpec = tween(600)) }

        val currentUser = repository.currentUser
        if (currentUser == null) {
            onDestinationReady("login")
        } else {
            val role = repository.getUserRole(currentUser.uid) ?: "DOG_OWNER"
            onDestinationReady(homeRouteForRole(role))
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier            = Modifier.alpha(alpha.value).scale(scale.value),
        ) {
            Icon(
                imageVector        = Icons.Filled.Pets,
                contentDescription = null,
                modifier           = Modifier.size(80.dp),
                tint               = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text  = "Pet4You",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text  = "Your pet care companion",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
