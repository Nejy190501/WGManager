package com.example.wgmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.wgmanager.data.*
import com.example.wgmanager.ui.components.*
import com.example.wgmanager.ui.navigation.AppScreen
import com.example.wgmanager.ui.theme.*

// Colors specific to Vault
private val VaultTeal = Color(0xFF0D9488)
private val VaultBannerBg = Color(0xFF1E3A5F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(onNavigate: (AppScreen) -> Unit, toast: ToastState) {
    val user = DataStore.currentUser ?: return
    val isAdmin = user.role != UserRole.USER
    var refreshKey by remember { mutableIntStateOf(0) }
    var showAddEntry by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<VaultItem?>(null) }

    val items = remember(refreshKey) { DataStore.vaultItems.toList() }
    val s = AppStrings

    Scaffold(
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { showAddEntry = true },
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) { Icon(Icons.Default.Add, "Add", modifier = Modifier.size(28.dp)) }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigate(AppScreen.DASHBOARD) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, s.back, tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.width(4.dp))
                Text("🛡️", fontSize = 24.sp)
                Spacer(Modifier.width(8.dp))
                Text(s.wgVault, color = MaterialTheme.colorScheme.onSurface, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            // Encrypted info banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = VaultBannerBg,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        s.encryptedInfo,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 24.dp),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Vault items list
            if (items.isEmpty()) {
                EmptyState(emoji = "🔐", message = s.noEntriesYet)
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    itemsIndexed(items, key = { _, i -> i.id }) { idx, item ->
                        AnimatedListItem(index = idx) {
                            VaultItemCard(item = item, toast = toast, isAdmin = isAdmin, onDelete = {
                                DataStore.removeVaultItem(item); toast.show(s.deletedItem); refreshKey++
                            }, onEdit = {
                                editingItem = item
                            })
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Add entry dialog
    if (showAddEntry) {
        VaultAddDialog(
            onDismiss = { showAddEntry = false },
            onAdd = { label, value, type, icon, secure ->
                DataStore.addVaultItem(VaultItem(label = label, value = value, type = type, customIcon = icon, isSecure = secure))
                toast.show(s.entryAddedVault); showAddEntry = false; refreshKey++
            }
        )
    }

    // Edit entry dialog
    editingItem?.let { item ->
        VaultAddDialog(
            existingItem = item,
            onDismiss = { editingItem = null },
            onAdd = { label, value, type, icon, secure ->
                item.label = label; item.value = value; item.type = type; item.customIcon = icon; item.isSecure = secure
                DataStore.updateVaultItem(item)
                toast.show(s.entryUpdatedVault); editingItem = null; refreshKey++
            }
        )
    }
}

@Composable
private fun VaultItemCard(item: VaultItem, toast: ToastState, isAdmin: Boolean, onDelete: () -> Unit, onEdit: () -> Unit = {}) {
    var revealed by remember { mutableStateOf(!item.isSecure) }
    val s = AppStrings

    val iconBg = when (item.type) {
        VaultType.WIFI -> Color(0xFF0EA5E9)
        VaultType.PHONE -> Color(0xFF10B981)
        VaultType.IBAN -> Color(0xFF6366F1)
        VaultType.CODE -> Color(0xFF64748B)
        VaultType.TEXT -> Color(0xFF8B5CF6)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon circle
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(iconBg.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.customIcon, fontSize = 20.sp)
            }

            Spacer(Modifier.width(14.dp))

            // Label + value
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.label.uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    if (revealed) item.value else "• • • • • • •",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (item.type == VaultType.WIFI) {
                    IconButton(onClick = { toast.show(s.qrShown) }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.QrCode, contentDescription = "QR", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                }
                if (item.isSecure) {
                    IconButton(onClick = { revealed = !revealed }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            if (revealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)
                        )
                    }
                }
                IconButton(onClick = { toast.show(s.copiedClipboard) }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
                if (isAdmin) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VaultAddDialog(onDismiss: () -> Unit, onAdd: (String, String, VaultType, String, Boolean) -> Unit, existingItem: VaultItem? = null) {
    val s = AppStrings
    var label by remember { mutableStateOf(existingItem?.label ?: "") }
    var value by remember { mutableStateOf(existingItem?.value ?: "") }
    var secure by remember { mutableStateOf(existingItem?.isSecure ?: false) }
    var type by remember { mutableStateOf(existingItem?.type ?: VaultType.TEXT) }
    var typeExpanded by remember { mutableStateOf(false) }

    val iconOptions = listOf(
        "📊" to VaultType.TEXT, "📞" to VaultType.PHONE, "💳" to VaultType.IBAN,
        "🏠" to VaultType.TEXT, "🎬" to VaultType.TEXT, "🎁" to VaultType.TEXT, "🔑" to VaultType.CODE
    )
    var selectedIcon by remember { mutableStateOf(existingItem?.customIcon ?: "🔒") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Title row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (existingItem != null) s.editEntryVault else s.newEntryVault, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Title + Icon
                Text(s.titleFieldLabel, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = label, onValueChange = { label = it },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp), singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedBorderColor = Color(0xFF60A5FA),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        placeholder = { Text("Netflix", color = MaterialTheme.colorScheme.outline) }
                    )
                    // Icon display
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(56.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(selectedIcon, fontSize = 24.sp)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Icon picker
                Text(s.chooseIconLabel, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    iconOptions.forEach { (emoji, _) ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .then(if (selectedIcon == emoji) Modifier.border(2.dp, Color(0xFF60A5FA), CircleShape) else Modifier)
                                .clickable { selectedIcon = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Category dropdown
                Text(s.categoryFieldLabel, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(6.dp))
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                    OutlinedTextField(
                        value = type.name, onValueChange = {}, readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedBorderColor = Color(0xFF60A5FA),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        VaultType.entries.forEach { t ->
                            DropdownMenuItem(text = { Text(t.name) }, onClick = { type = t; typeExpanded = false })
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Content/Value
                Text(s.contentFieldLabel, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = value, onValueChange = { value = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedBorderColor = Color(0xFF60A5FA),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    placeholder = { Text(". . .", color = MaterialTheme.colorScheme.outline) }
                )

                Spacer(Modifier.height(12.dp))

                // Secure checkbox
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = secure, onCheckedChange = { secure = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF2563EB),
                            uncheckedColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Text(s.hiddenLabel, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                }

                Spacer(Modifier.height(16.dp))

                // Save button
                Button(
                    onClick = {
                        if (label.isNotBlank() && value.isNotBlank()) {
                            onAdd(label, value, type, selectedIcon, secure)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(s.save, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
