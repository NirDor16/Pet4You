package com.example.pet4you.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pet4you.data.model.DOG_BREEDS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Pet4YouTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(
                text  = title,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            }
        },
        actions = { actions() },
        colors  = TopAppBarDefaults.topAppBarColors(
            containerColor       = MaterialTheme.colorScheme.surface,
            titleContentColor    = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor     = MaterialTheme.colorScheme.onSurface,
        ),
    )
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier.fillMaxSize()) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("lottie_loading.json"))
    val progress    by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (composition != null) {
            LottieAnimation(composition, { progress }, modifier = Modifier.size(100.dp))
        } else {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun SuccessOverlay(message: String = "Saved!") {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("lottie_success.json"))
    val progress    by animateLottieCompositionAsState(composition, iterations = 1)
    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LottieAnimation(composition, { progress }, modifier = Modifier.size(140.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                text  = message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String = "",
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("lottie_empty.json"))
    val progress    by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                LottieAnimation(composition, { progress }, modifier = Modifier.size(120.dp))
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    modifier           = Modifier.size(44.dp),
                    tint               = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text      = title,
                style     = MaterialTheme.typography.titleMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (subtitle.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text      = subtitle,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.padding(horizontal = 32.dp),
                )
            }
        }
    }
}

@Composable
fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text      = message,
            color     = MaterialTheme.colorScheme.error,
            style     = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(16.dp),
        )
    }
}

@Composable
fun StatusBadge(label: String, containerColor: androidx.compose.ui.graphics.Color, contentColor: androidx.compose.ui.graphics.Color) {
    Surface(
        shape         = MaterialTheme.shapes.extraSmall,
        color         = containerColor,
        contentColor  = contentColor,
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun Pet4YouCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    if (onClick != null) {
        ElevatedCard(
            onClick   = onClick,
            modifier  = modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            colors    = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) { content() }
    } else {
        ElevatedCard(
            modifier  = modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            colors    = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) { content() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedSelector(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Breed",
    onBreedSelected: (String) -> Unit = onValueChange,
) {
    var expanded by remember { mutableStateOf(false) }
    val filtered = remember(value) {
        if (value.isBlank()) emptyList()
        else DOG_BREEDS.filter { it.contains(value, ignoreCase = true) }.take(8)
    }
    ExposedDropdownMenuBox(
        expanded = expanded && filtered.isNotEmpty(),
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value         = value,
            onValueChange = { onValueChange(it); expanded = true },
            label         = { Text(label) },
            leadingIcon   = { Icon(Icons.Filled.Pets, contentDescription = null) },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && filtered.isNotEmpty()) },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(
            expanded = expanded && filtered.isNotEmpty(),
            onDismissRequest = { expanded = false },
        ) {
            filtered.forEach { breed ->
                DropdownMenuItem(
                    text    = { Text(breed) },
                    onClick = { onBreedSelected(breed); expanded = false },
                )
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    tint: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
) {
    val resolvedTint = if (tint == androidx.compose.ui.graphics.Color.Unspecified) MaterialTheme.colorScheme.primary else tint
    Row(
        modifier              = modifier,
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            modifier           = Modifier.size(18.dp),
            tint               = resolvedTint,
        )
        Text(
            text  = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun PawBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val bg = MaterialTheme.colorScheme.background
    val top = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(listOf(top, bg))
        ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pawColor = androidx.compose.ui.graphics.Color(0xFF006B5B).copy(alpha = 0.05f)
            listOf(
                Offset(size.width * 0.08f, size.height * 0.10f) to  30f,
                Offset(size.width * 0.90f, size.height * 0.15f) to -20f,
                Offset(size.width * 0.15f, size.height * 0.42f) to  45f,
                Offset(size.width * 0.85f, size.height * 0.50f) to -35f,
                Offset(size.width * 0.05f, size.height * 0.75f) to  15f,
                Offset(size.width * 0.92f, size.height * 0.80f) to -45f,
                Offset(size.width * 0.50f, size.height * 0.08f) to   0f,
                Offset(size.width * 0.60f, size.height * 0.90f) to  25f,
            ).forEach { (center, angle) ->
                drawPawPrint(center, size.minDimension * 0.040f, pawColor, angle)
            }
        }
        content()
    }
}

private fun DrawScope.drawPawPrint(
    center: Offset,
    padRadius: Float,
    color: androidx.compose.ui.graphics.Color,
    rotationDeg: Float,
) {
    rotate(rotationDeg, pivot = center) {
        drawCircle(color, radius = padRadius, center = center)
        val toeR   = padRadius * 0.48f
        val spread = padRadius * 1.15f
        listOf(
            Offset(center.x - spread * 0.80f, center.y - spread * 1.00f),
            Offset(center.x - spread * 0.25f, center.y - spread * 1.35f),
            Offset(center.x + spread * 0.25f, center.y - spread * 1.35f),
            Offset(center.x + spread * 0.80f, center.y - spread * 1.00f),
        ).forEach { drawCircle(color, radius = toeR, center = it) }
    }
}

@Composable
fun SectionHero(
    icon: ImageVector,
    title: String,
    subtitle: String,
    containerColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(containerColor, containerColor.copy(alpha = 0.25f))
                )
            )
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = iconTint,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = iconTint.copy(alpha = 0.7f),
                )
            }
            Box(
                modifier         = Modifier
                    .size(68.dp)
                    .background(iconTint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = iconTint,
                    modifier           = Modifier.size(38.dp),
                )
            }
        }
    }
}

@Composable
fun SectionBanner(
    icon: ImageVector,
    subtitle: String,
    containerColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .background(containerColor.copy(alpha = 0.55f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier         = Modifier
                .size(36.dp)
                .background(iconTint.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = iconTint,
                modifier           = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text  = subtitle,
            style = MaterialTheme.typography.labelLarge,
            color = iconTint,
        )
    }
}
