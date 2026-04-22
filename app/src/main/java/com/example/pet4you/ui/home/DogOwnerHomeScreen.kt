package com.example.pet4you.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DogOwnerHomeScreen(
    onLogout: () -> Unit,
    onNavigateToDogs: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToMeetups: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome, Dog Owner!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Manage your dogs, reminders and meetups", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(32.dp))

        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onNavigateToDogs() }) {
            Text("My Dogs", modifier = Modifier.padding(16.dp))
        }
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onNavigateToReminders() }) {
            Text("Reminders", modifier = Modifier.padding(16.dp))
        }
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onNavigateToMeetups() }) {
            Text("Meetups", modifier = Modifier.padding(16.dp))
        }
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Text("AI Chat", modifier = Modifier.padding(16.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(onClick = onLogout) {
            Text("Logout")
        }
    }
}
