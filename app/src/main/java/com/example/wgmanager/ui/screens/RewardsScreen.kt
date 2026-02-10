package com.example.wgmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.wgmanager.data.*
import com.example.wgmanager.ui.components.*
import com.example.wgmanager.ui.navigation.AppScreen
import com.example.wgmanager.ui.theme.*

private val RewardOrange = Color(0xFFF59E0B)
private val RewardOrangeLight = Color(0xFFFBBF24)

@Composable
fun RewardsScreen(onNavigate: (AppScreen) -> Unit, toast: ToastState) {
    val user = DataStore.currentUser ?: return
    val isAdmin = user.role != UserRole.USER
    var refreshKey by remember { mutableIntStateOf(0) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Angebote, 1 = Mein Inventar
    var selectedCategory by remember { mutableStateOf("Alle") }
    var selectedReward by remember { mutableStateOf<RewardItem?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    val rewards = remember(refreshKey) { DataStore.rewards.toList() }
    val categories = listOf("Alle", "Haushalt", "Spaß", "Essen")

    // Calculate next goal
    val nextReward = rewards.filter { it.cost > user.points }.minByOrNull { it.cost }
    val pointsLeft = (nextReward?.cost ?: 0) - user.points

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Orange gradient header
        Box(modifier = Modifier.fillMaxWidth().background(
            Brush.linearGradient(listOf(RewardOrange, RewardOrangeLight))
        )) {
            // Background gift icon watermark
            Text("🎁", fontSize = 120.sp, modifier = Modifier.align(Alignment.TopEnd).offset(x = 30.dp, y = (-20).dp).graphicsLayer(alpha = 0.15f))
            
            Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onNavigate(AppScreen.DASHBOARD) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("Prämien Shop", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    // User badge
                    Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.2f)) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(user.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.width(6.dp))
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF22C55E)))
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                // Points balance card
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.2f)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("DEIN KONTOSTAND", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text("${user.points}", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Punkte", fontSize = 16.sp, color = Color.White, modifier = Modifier.offset(y = (-6).dp))
                                }
                            }
                            Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(48.dp)) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(Icons.Default.CreditCard, null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                        if (nextReward != null) {
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("NÄCHSTES ZIEL: ${nextReward.title.uppercase()}", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("$pointsLeft LEFT", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (user.points.toFloat() / nextReward.cost).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF22C55E),
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                // Tabs
                Surface(shape = RoundedCornerShape(30.dp), color = Color.White.copy(alpha = 0.15f)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                        RewardTabPill("🎁 Angebote", selectedTab == 0) { selectedTab = 0 }
                        RewardTabPill("🏷️ Mein Inventar", selectedTab == 1) { selectedTab = 1 }
                    }
                }
            }
        }

        // Category filters
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { cat ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (selectedCategory == cat) RewardOrange else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.clickable { selectedCategory = cat }
                ) {
                    Text(cat, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Rewards grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            items(rewards, key = { it.id }) { reward ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth().clickable { selectedReward = reward }
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        // Emoji in circle
                        Text(reward.emoji, fontSize = 40.sp)
                        Spacer(Modifier.height(10.dp))
                        Text(reward.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center, maxLines = 2)
                        Spacer(Modifier.height(8.dp))
                        // Cost badge
                        val canAfford = user.points >= reward.cost
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (canAfford) RewardOrange.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                            border = if (canAfford) null else ButtonDefaults.outlinedButtonBorder(enabled = true)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (!canAfford) {
                                    Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(12.dp))
                                    Spacer(Modifier.width(4.dp))
                                }
                                Text("${reward.cost} Punkte", color = if (canAfford) RewardOrange else MaterialTheme.colorScheme.outline, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        DashboardBottomNav(current = AppScreen.REWARDS, onNavigate = onNavigate)
    }

    // FAB
    if (isAdmin) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = RewardOrange,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(end = 24.dp, bottom = 90.dp).size(56.dp)
            ) { Icon(Icons.Default.Add, "Add", modifier = Modifier.size(28.dp)) }
        }
    }

    // Reward detail dialog
    selectedReward?.let { reward ->
        Dialog(onDismissRequest = { selectedReward = null }) {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF374151)) {
                            Text("DETAILS", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { selectedReward = null }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // Emoji in orange ring
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(90.dp)
                        .border(3.dp, RewardOrange, CircleShape).clip(CircleShape)) {
                        Text(reward.emoji, fontSize = 44.sp)
                    }
                    Spacer(Modifier.height(16.dp))

                    Text(reward.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(8.dp))

                    // Cost badge
                    Surface(shape = RoundedCornerShape(12.dp), color = RewardOrange.copy(alpha = 0.15f), border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = Brush.linearGradient(listOf(RewardOrange, RewardOrange)))) {
                        Text("${reward.cost} Punkte", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = RewardOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(12.dp))

                    Text(reward.description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(20.dp))

                    // Admin buttons
                    if (isAdmin) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF374151),
                                modifier = Modifier.weight(1f).clickable { toast.show("Bearbeiten..."); selectedReward = null }
                            ) {
                                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Bearbeiten", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF374151),
                                modifier = Modifier.weight(1f).clickable {
                                    DataStore.removeReward(reward)
                                    FirebaseSync.removeReward(reward.id)
                                    toast.show("Gelöscht! 🗑️")
                                    refreshKey++
                                    selectedReward = null
                                }
                            ) {
                                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Löschen", color = Color(0xFFEF4444), fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    // Buy button
                    val canAfford = user.points >= reward.cost
                    Button(
                        onClick = {
                            if (DataStore.redeemReward(reward)) {
                                toast.show("${reward.emoji} ${reward.title} eingelöst! 🎉")
                                refreshKey++
                                selectedReward = null
                            } else {
                                toast.show("Nicht genug Punkte! 😢", ToastType.ERROR)
                            }
                        },
                        enabled = canAfford,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RewardOrange,
                            disabledContainerColor = Color(0xFF374151)
                        ),
                        contentPadding = PaddingValues()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Kaufen", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (canAfford) Color.White else MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.CheckCircle, null, tint = if (canAfford) Color.White else MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }

    // Add dialog
    if (showAddDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newCost by remember { mutableStateOf("") }
        var newDesc by remember { mutableStateOf("") }
        var selectedEmoji by remember { mutableStateOf("🎁") }
        val emojis = listOf("🎁", "🎬", "🍕", "🛋️", "🧹", "👑", "🎮", "🎂", "☕", "🎵", "🚿", "🗑️")

        Dialog(onDismissRequest = { showAddDialog = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Neue Belohnung", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = { showAddDialog = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // Emoji picker
                    Text("ICON", color = MaterialTheme.colorScheme.outline, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        emojis.take(6).forEach { emoji ->
                            Surface(
                                shape = CircleShape,
                                color = if (selectedEmoji == emoji) RewardOrange.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                border = if (selectedEmoji == emoji) ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = Brush.linearGradient(listOf(RewardOrange, RewardOrange))) else null,
                                modifier = Modifier.size(44.dp).clickable { selectedEmoji = emoji }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(emoji, fontSize = 20.sp)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        emojis.drop(6).forEach { emoji ->
                            Surface(
                                shape = CircleShape,
                                color = if (selectedEmoji == emoji) RewardOrange.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                border = if (selectedEmoji == emoji) ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = Brush.linearGradient(listOf(RewardOrange, RewardOrange))) else null,
                                modifier = Modifier.size(44.dp).clickable { selectedEmoji = emoji }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(emoji, fontSize = 20.sp)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = newTitle, onValueChange = { newTitle = it },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                        placeholder = { Text("TITEL", color = MaterialTheme.colorScheme.outline) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFF475569), focusedBorderColor = RewardOrange,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = RewardOrange
                        )
                    )
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newCost, onValueChange = { newCost = it },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                        placeholder = { Text("KOSTEN", color = MaterialTheme.colorScheme.outline) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFF475569), focusedBorderColor = RewardOrange,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = RewardOrange
                        )
                    )
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newDesc, onValueChange = { newDesc = it },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), minLines = 2,
                        placeholder = { Text("BESCHREIBUNG", color = MaterialTheme.colorScheme.outline) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFF475569), focusedBorderColor = RewardOrange,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedTextColor = MaterialTheme.colorScheme.onSurface, cursorColor = RewardOrange
                        )
                    )
                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val cost = newCost.toIntOrNull() ?: return@Button
                            val reward = RewardItem(
                                id = "r${System.currentTimeMillis()}",
                                title = newTitle,
                                emoji = selectedEmoji,
                                cost = cost,
                                description = newDesc
                            )
                            DataStore.addReward(reward)
                            FirebaseSync.pushReward(reward)
                            toast.show("Belohnung erstellt! 🎉")
                            refreshKey++
                            showAddDialog = false
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RewardOrange)
                    ) {
                        Text("Erstellen", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.RewardTabPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = if (selected) Color.White else Color.Transparent,
        modifier = Modifier.weight(1f).clickable(onClick = onClick)
    ) {
        Text(label, modifier = Modifier.padding(vertical = 10.dp), textAlign = TextAlign.Center,
            color = if (selected) Color.Black else Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}


