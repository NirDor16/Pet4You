package com.example.pet4you.ui.dog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pet4you.data.model.*
import com.example.pet4you.ui.components.DogAvatarCanvas
import com.example.pet4you.ui.components.Pet4YouTopBar
import com.google.gson.Gson

private val gson = Gson()

private val AvatarTabs = listOf("Body", "Color", "Ears", "Tail", "Extras")

private val FurColorMap = listOf(
    FurColor.GOLDEN  to Color(0xFFD4A017),
    FurColor.BLACK   to Color(0xFF2C2C2C),
    FurColor.WHITE   to Color(0xFFF0EDE8),
    FurColor.BROWN   to Color(0xFF8B5E3C),
    FurColor.GRAY    to Color(0xFF8B8B8B),
    FurColor.SPOTTED to Color(0xFFF0EDE8)
)

@Composable
fun DogAvatarScreen(
    breed: String,
    initialAvatarJson: String,
    onSave: (avatarJson: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val startAvatar = remember(initialAvatarJson, breed) {
        if (initialAvatarJson.isNotEmpty())
            runCatching { gson.fromJson(initialAvatarJson, DogAvatar::class.java) }.getOrNull()
                ?: breedPreset(breed)
        else breedPreset(breed)
    }

    var avatar by remember { mutableStateOf(startAvatar) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            Pet4YouTopBar(
                title  = if (breed.isNotEmpty()) "$breed Avatar" else "Customize Avatar",
                onBack = onNavigateBack
            )
        },
        bottomBar = {
            Box(Modifier.fillMaxWidth().padding(16.dp)) {
                Button(
                    onClick   = { onSave(gson.toJson(avatar)) },
                    modifier  = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Save Avatar", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier            = Modifier.fillMaxSize().padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            DogAvatarCanvas(
                avatar   = avatar,
                animated = true,
                modifier = Modifier.size(200.dp)
            )
            Spacer(Modifier.height(16.dp))
            TabRow(selectedTabIndex = selectedTab) {
                AvatarTabs.forEachIndexed { i, title ->
                    Tab(
                        selected = selectedTab == i,
                        onClick  = { selectedTab = i },
                        text     = { Text(title, style = MaterialTheme.typography.labelLarge) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            when (selectedTab) {
                0 -> BodyTab(avatar.bodyShape)   { avatar = avatar.copy(bodyShape = it) }
                1 -> ColorTab(avatar.furColor)   { avatar = avatar.copy(furColor  = it) }
                2 -> EarsTab(avatar.earType)     { avatar = avatar.copy(earType   = it) }
                3 -> TailTab(avatar.tailType)    { avatar = avatar.copy(tailType  = it) }
                4 -> ExtrasTab(avatar.accessory) { avatar = avatar.copy(accessory = it) }
            }
        }
    }
}

@Composable
private fun <T> OptionRow(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(options) { (value, label) ->
            val isSelected = value == selected
            Surface(
                onClick   = { onSelect(value) },
                shape     = RoundedCornerShape(12.dp),
                color     = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant,
                modifier  = Modifier
                    .height(48.dp)
                    .widthIn(min = 80.dp)
                    .then(
                        if (isSelected) Modifier.border(
                            2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)
                        ) else Modifier
                    )
            ) {
                Box(Modifier.fillMaxSize().padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text  = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BodyTab(selected: BodyShape, onSelect: (BodyShape) -> Unit) {
    OptionRow(
        options  = listOf(BodyShape.ROUND to "Round", BodyShape.STOCKY to "Stocky", BodyShape.SLIM to "Slim"),
        selected = selected,
        onSelect = onSelect
    )
}

@Composable
private fun EarsTab(selected: EarType, onSelect: (EarType) -> Unit) {
    OptionRow(
        options  = listOf(EarType.FLOPPY to "Floppy", EarType.POINTY to "Pointy", EarType.ROUND to "Round"),
        selected = selected,
        onSelect = onSelect
    )
}

@Composable
private fun TailTab(selected: TailType, onSelect: (TailType) -> Unit) {
    OptionRow(
        options  = listOf(TailType.STRAIGHT to "Straight", TailType.CURLY to "Curly", TailType.STUB to "Stub"),
        selected = selected,
        onSelect = onSelect
    )
}

@Composable
private fun ExtrasTab(selected: Accessory, onSelect: (Accessory) -> Unit) {
    OptionRow(
        options  = listOf(
            Accessory.NONE    to "None",
            Accessory.COLLAR  to "Collar",
            Accessory.BOW     to "Bow",
            Accessory.HAT     to "Hat",
            Accessory.BANDANA to "Bandana"
        ),
        selected = selected,
        onSelect = onSelect
    )
}

@Composable
private fun ColorTab(selected: FurColor, onSelect: (FurColor) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(FurColorMap) { (fur, color) ->
            val isSelected = fur == selected
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onSelect(fur) }
                    .then(
                        if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        else Modifier
                    )
            ) {
                if (fur == FurColor.SPOTTED) {
                    Box(
                        Modifier
                            .size(14.dp)
                            .padding(start = 10.dp, top = 8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B5E3C).copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}
