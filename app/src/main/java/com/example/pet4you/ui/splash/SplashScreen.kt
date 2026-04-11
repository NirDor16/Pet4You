package com.example.pet4you.ui.splash

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.pet4you.repository.AuthRepository
import com.example.pet4you.ui.navigation.homeRouteForRole
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onDestinationReady: (route: String) -> Unit
) {
    val repository = remember { AuthRepository() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            val currentUser = repository.currentUser
            if (currentUser == null) {
                onDestinationReady("login")
            } else {
                val role = repository.getUserRole(currentUser.uid) ?: "DOG_OWNER"
                onDestinationReady(homeRouteForRole(role))
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
