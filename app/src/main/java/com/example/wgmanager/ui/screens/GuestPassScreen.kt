package com.example.wgmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.wgmanager.data.*
import com.example.wgmanager.ui.components.*
import com.example.wgmanager.ui.navigation.AppScreen
import com.example.wgmanager.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestPassScreen(
    onNavigate: (AppScreen) -> Unit,
    toast: ToastState
) {
    val palette = LocalThemePalette.current
    val str = AppStrings
    val activePasses = DataStore.getActiveGuestPasses()
    val allPasses = DataStore.guestPasses.filter {
        it.wgId == (DataStore.currentUser?.wgId ?: "")
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedPass by remember { mutableStateOf<GuestPass?>(null) }
    var refreshKey by remember { mutableIntStateOf(0) }

    key(refreshKey) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text("🎫 ${str.guestPass}", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { onNavigate(AppScreen.DASHBOARD) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showCreateDialog = true }) {
                            Icon(Icons.Default.Add, "Create", tint = palette.accent)
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
                // Header card
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
                                        listOf(Color(0xFF7C3AED), Color(0xFFDB2777))
                                    ),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🎫", fontSize = 40.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    str.guestPassSubtitle,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${activePasses.size} ${str.activeSuffix}",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Section header
                item {
                    Text(
                        str.activePassesTitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (allPasses.isEmpty()) {
                    item {
                        EmptyState(
                            emoji = "🎫",
                            message = str.noActivePasses
                        )
                    }
                } else {
                    itemsIndexed(allPasses) { index, pass ->
                        AnimatedListItem(index = index) {
                            GuestPassCard(
                                pass = pass,
                                onView = { selectedPass = pass },
                                onRevoke = {
                                    DataStore.revokeGuestPass(pass)
                                    toast.show(str.passRevoked)
                                    refreshKey++
                                },
                                onRemove = {
                                    DataStore.removeGuestPass(pass)
                                    refreshKey++
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Create dialog
    if (showCreateDialog) {
        CreateGuestPassDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                DataStore.createGuestPass(name)
                toast.show(str.passCreated)
                showCreateDialog = false
                refreshKey++
            }
        )
    }

    // Detail dialog (QR simulation)
    selectedPass?.let { pass ->
        GuestPassDetailDialog(
            pass = pass,
            onDismiss = { selectedPass = null }
        )
    }
}

@Composable
private fun GuestPassCard(
    pass: GuestPass,
    onView: () -> Unit,
    onRevoke: () -> Unit,
    onRemove: () -> Unit
) {
    val palette = LocalThemePalette.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (pass.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        onClick = onView
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (pass.isActive) palette.accent.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(if (pass.isActive) "🎫" else "❌", fontSize = 22.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    pass.guestName,
                    color = if (pass.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    "${AppStrings.createdByLabel}: ${pass.createdBy}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Text(
                    pass.createdDate,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 11.sp
                )
            }

            if (pass.isActive) {
                // Code preview
                Text(
                    pass.accessCode,
                    color = palette.accent,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onRevoke, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, "Revoke", tint = WGDanger.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                }
            } else {
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun GuestPassDetailDialog(
    pass: GuestPass,
    onDismiss: () -> Unit
) {
    val palette = LocalThemePalette.current
    val str = AppStrings

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎫", fontSize = 36.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    pass.guestName,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Spacer(Modifier.height(16.dp))

                // Simulated QR code block
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // ASCII art QR simulation
                        Text(
                            "▓▓▓▓▓▓▓░▓░▓▓▓▓▓▓▓\n" +
                            "▓░░░░░▓░▓░▓░░░░░▓\n" +
                            "▓░▓▓▓░▓░░░▓░▓▓▓░▓\n" +
                            "▓░▓▓▓░▓░▓░▓░▓▓▓░▓\n" +
                            "▓░░░░░▓░▓░▓░░░░░▓\n" +
                            "▓▓▓▓▓▓▓░▓░▓▓▓▓▓▓▓\n" +
                            "░░░░░░░░▓░░░░░░░░\n" +
                            "▓▓▓░▓▓▓▓░▓▓▓░▓▓▓░\n" +
                            "▓▓▓▓▓▓▓░▓▓░▓▓▓▓░▓",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            lineHeight = 9.sp,
                            color = Color.Black
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            pass.accessCode,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Info rows
                Text(
                    str.guestInfo,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))

                InfoRow(label = str.accessCode, value = pass.accessCode, palette = palette)
                if (pass.wifiPassword.isNotEmpty()) {
                    InfoRow(label = str.wifiPassword, value = pass.wifiPassword, palette = palette)
                }
                InfoRow(label = str.createdByLabel, value = pass.createdBy, palette = palette)

                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onDismiss) {
                    Text(str.close, color = palette.accent)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, palette: ThemePalette) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Text(
            value,
            color = palette.accent,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun CreateGuestPassDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    val palette = LocalThemePalette.current
    val str = AppStrings
    var name by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(str.createPass, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(str.guestName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.accent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = palette.accent,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                        onClick = { if (name.isNotBlank()) onCreate(name) },
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
