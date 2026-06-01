package com.example.pet4you.ui.serviceprovider

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.example.pet4you.data.model.ProviderType
import com.example.pet4you.ui.components.ErrorMessage
import com.example.pet4you.ui.components.LoadingBox
import com.example.pet4you.ui.components.PawBackground
import com.example.pet4you.ui.components.Pet4YouTopBar
import com.example.pet4you.ui.components.SectionBanner
import com.example.pet4you.ui.theme.DeepBlue
import com.example.pet4you.ui.theme.SoftBlue
import com.example.pet4you.viewmodel.ProfileActionState
import com.example.pet4you.viewmodel.ProfileState
import com.example.pet4you.viewmodel.ServiceProviderViewModel

@Composable
fun ServiceProviderProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ServiceProviderViewModel = viewModel(),
) {
    val profileState       by viewModel.profileState.collectAsState()
    val profileActionState by viewModel.profileActionState.collectAsState()

    var fullName    by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location    by remember { mutableStateOf("") }
    var isAvailable by remember { mutableStateOf(true) }
    var email       by remember { mutableStateOf("") }
    var providerType by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadProfile() }
    LaunchedEffect(profileState) {
        if (profileState is ProfileState.Loaded) {
            val p = (profileState as ProfileState.Loaded).provider
            fullName    = p.fullName
            description = p.description
            location    = p.location
            isAvailable = p.isAvailable
            email       = p.email
            providerType = p.providerType
        }
    }
    LaunchedEffect(profileActionState) {
        if (profileActionState is ProfileActionState.Success) { viewModel.resetActionState(); onNavigateBack() }
    }

    val isLoading    = profileActionState is ProfileActionState.Loading
    val errorMessage = (profileActionState as? ProfileActionState.Error)?.message

    Scaffold(
        topBar = { Pet4YouTopBar(title = "My Profile", onBack = onNavigateBack) },
    ) { padding ->
        when (profileState) {
            is ProfileState.Loading, ProfileState.Idle -> LoadingBox(modifier = Modifier.padding(padding))
            is ProfileState.Error -> ErrorMessage(
                message  = (profileState as ProfileState.Error).message,
                modifier = Modifier.padding(padding),
            )
            else -> {
                PawBackground(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    SectionBanner(
                        icon           = Icons.Filled.Person,
                        subtitle       = "Your professional profile",
                        containerColor = SoftBlue,
                        iconTint       = DeepBlue,
                    )
                    Column(
                        modifier            = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                    Surface(
                        color  = MaterialTheme.colorScheme.surfaceVariant,
                        shape  = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier            = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Work,
                                    contentDescription = null,
                                    modifier           = Modifier.size(16.dp),
                                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text     = "  ${ProviderType.displayName(providerType)}",
                                    style    = MaterialTheme.typography.bodyMedium,
                                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Email,
                                    contentDescription = null,
                                    modifier           = Modifier.size(16.dp),
                                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text  = "  $email",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value         = fullName,
                        onValueChange = { fullName = it },
                        label         = { Text("Full Name") },
                        leadingIcon   = { Icon(Icons.Filled.Person, contentDescription = null) },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true,
                    )

                    OutlinedTextField(
                        value         = description,
                        onValueChange = { description = it },
                        label         = { Text("Description") },
                        leadingIcon   = { Icon(Icons.Filled.Notes, contentDescription = null) },
                        modifier      = Modifier.fillMaxWidth(),
                        minLines      = 3,
                    )

                    OutlinedTextField(
                        value         = location,
                        onValueChange = { location = it },
                        label         = { Text("Location") },
                        leadingIcon   = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true,
                    )

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Text("Available for new clients", style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = isAvailable, onCheckedChange = { isAvailable = it })
                    }

                    if (errorMessage != null) {
                        Text(
                            text  = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Button(
                        onClick  = { viewModel.updateProfile(fullName, description, location, isAvailable) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled  = !isLoading && fullName.isNotBlank(),
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Save Changes", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    }   // closes inner Column(padding 24dp)
                }       // closes outer Column(scroll)
                }       // closes PawBackground
            }
        }
    }
}
