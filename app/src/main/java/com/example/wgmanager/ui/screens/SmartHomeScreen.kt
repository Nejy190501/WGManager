package com.example.wgmanager.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.wgmanager.data.*
import com.example.wgmanager.ui.components.*
import com.example.wgmanager.ui.navigation.AppScreen
import com.example.wgmanager.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartHomeScreen(
    onNavigate: (AppScreen) -> Unit,
    toast: ToastState
) {
    val palette = LocalThemePalette.current
    val str = AppStrings
    val scenes = DataStore.smartScenes
    var showAddDialog by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableIntStateOf(0) }

    key(refreshKey) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text("🏠 ${str.smartHome}", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { onNavigate(AppScreen.DASHBOARD) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, "Add", tint = palette.accent)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Header
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF06B6D4), Color(0xFF8B5CF6))
                                    ),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🏠", fontSize = 40.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    str.smartHomeSubtitle,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                val activeCount = scenes.count { it.isActive }
                                if (activeCount > 0) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "$activeCount ${str.activeSuffix}",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick Actions Grid (2 per row)
                item {
                    Text(
                        str.scenesTitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Scene cards
                itemsIndexed(scenes) { index, scene ->
                    AnimatedListItem(index = index) {
                        SceneCard(
                            scene = scene,
                            onToggle = {
                                DataStore.toggleSmartScene(scene)
                                val msg = if (scene.isActive) str.sceneActivated else str.sceneDeactivated
                                toast.show(msg)
                                refreshKey++
                            },
                            onRemove = {
                                DataStore.removeSmartScene(scene)
                                refreshKey++
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSceneDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, emoji, desc, notif ->
                DataStore.addSmartScene(name, emoji, desc, notif)
                toast.show(str.sceneAdded)
                showAddDialog = false
                refreshKey++
            }
        )
    }
}

@Composable
private fun SceneCard(
    scene: SmartScene,
    onToggle: () -> Unit,
    onRemove: () -> Unit
) {
    val str = AppStrings
    val palette = LocalThemePalette.current
    val bgColor by animateColorAsState(
        if (scene.isActive) palette.accent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        label = "sceneBg"
    )
    val borderColor by animateColorAsState(
        if (scene.isActive) palette.accent.copy(alpha = 0.4f) else Color.Transparent,
        label = "sceneBorder"
    )

    // Pulse animation when active
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (scene.isActive) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = EaseInOutCubic),
            RepeatMode.Reverse
        ),
        label = "emojiPulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (scene.isActive) Modifier.border(1.dp, borderColor, RoundedCornerShape(16.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji with pulse
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        if (scene.isActive) palette.accent.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(scene.emoji, fontSize = 28.sp)
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    scene.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    scene.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                if (scene.isActive) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(WGSuccess)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(str.activeUpper, color = WGSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Switch(
                    checked = scene.isActive,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = palette.accent,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.Delete, "Remove",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddSceneDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String) -> Unit
) {
    val palette = LocalThemePalette.current
    val str = AppStrings
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("🎬") }
    var description by remember { mutableStateOf("") }
    var notification by remember { mutableStateOf("") }

    val emojiOptions = listOf("🎬", "🎉", "📚", "🌙", "☀️", "🎮", "🧘", "🍳", "💪", "🧹")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(str.addScene, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(16.dp))

                // Emoji picker
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    emojiOptions.forEach { e ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (emoji == e) palette.accent.copy(alpha = 0.3f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                                .clickable { emoji = e },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(e, fontSize = 18.sp)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = palette.accent,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = palette.accent,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(str.sceneName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors
                )

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(str.sceneDescription) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors
                )

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = notification,
                    onValueChange = { notification = it },
                    label = { Text(str.notificationText) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors
                )

                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(str.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onAdd(
                                    name, emoji,
                                    description.ifBlank { name },
                                    notification.ifBlank { "$emoji $name activated!" }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(str.save, color = Color.White)
                    }
                }
            }
        }
    }
}
