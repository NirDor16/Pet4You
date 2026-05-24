package com.example.pet4you.ui.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.pet4you.repository.AuthRepository
import com.example.pet4you.ui.navigation.homeRouteForRole
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onDestinationReady: (route: String) -> Unit,
) {
    val repository  = remember { AuthRepository() }
    val scope       = rememberCoroutineScope()
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("lottie_dog.json"))
    val progress    by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    LaunchedEffect(Unit) {
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
        ) {
            LottieAnimation(
                composition = composition,
                progress    = { progress },
                modifier    = Modifier.size(220.dp),
            )
            Spacer(Modifier.height(8.dp))
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
