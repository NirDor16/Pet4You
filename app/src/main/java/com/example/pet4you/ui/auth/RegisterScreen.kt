package com.example.pet4you.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pet4you.data.model.ProviderType
import com.example.pet4you.data.model.UserRole
import com.example.pet4you.viewmodel.AuthState
import com.example.pet4you.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: (role: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
) {
    var fullName             by remember { mutableStateOf("") }
    var email                by remember { mutableStateOf("") }
    var password             by remember { mutableStateOf("") }
    var selectedRole         by remember { mutableStateOf(UserRole.DOG_OWNER) }
    var selectedProviderType by remember { mutableStateOf(ProviderType.VET) }
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            val role = (authState as AuthState.Success).role
            authViewModel.resetState()
            onRegisterSuccess(role)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── Brand header ──────────────────────────────────────────────────────
        Icon(
            imageVector        = Icons.Filled.Pets,
            contentDescription = null,
            modifier           = Modifier.size(64.dp),
            tint               = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text  = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text  = "Join Pet4You today",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(32.dp))

        // ── Fields ────────────────────────────────────────────────────────────
        OutlinedTextField(
            value         = fullName,
            onValueChange = { fullName = it },
            label         = { Text("Full Name") },
            leadingIcon   = { Icon(Icons.Filled.Person, contentDescription = null) },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value           = email,
            onValueChange   = { email = it },
            label           = { Text("Email") },
            leadingIcon     = { Icon(Icons.Filled.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine      = true,
            modifier        = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value                = password,
            onValueChange        = { password = it },
            label                = { Text("Password") },
            leadingIcon          = { Icon(Icons.Filled.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine           = true,
            modifier             = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(20.dp))

        // ── Role selection ────────────────────────────────────────────────────
        Text("I am a:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FilterChip(
                selected = selectedRole == UserRole.DOG_OWNER,
                onClick  = { selectedRole = UserRole.DOG_OWNER },
                label    = { Text("Dog Owner") },
                modifier = Modifier.weight(1f),
            )
            FilterChip(
                selected = selectedRole == UserRole.SERVICE_PROVIDER,
                onClick  = { selectedRole = UserRole.SERVICE_PROVIDER },
                label    = { Text("Service Provider") },
                modifier = Modifier.weight(1f),
            )
        }

        if (selectedRole == UserRole.SERVICE_PROVIDER) {
            Spacer(Modifier.height(16.dp))
            Text(
                text  = "Type of service:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProviderType.all.forEach { type ->
                    FilterChip(
                        selected = selectedProviderType == type,
                        onClick  = { selectedProviderType = type },
                        label    = { Text(ProviderType.displayName(type)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        if (authState is AuthState.Error) {
            Text(
                text  = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick  = {
                val providerType = if (selectedRole == UserRole.SERVICE_PROVIDER) selectedProviderType else null
                authViewModel.register(fullName, email, password, selectedRole, providerType)
            },
            enabled  = authState !is AuthState.Loading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color       = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text("Register", style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Login")
        }
    }
}
