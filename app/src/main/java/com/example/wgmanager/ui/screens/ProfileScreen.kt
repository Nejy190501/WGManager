package com.example.wgmanager.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wgmanager.data.*
import com.example.wgmanager.ui.components.*
import com.example.wgmanager.ui.navigation.AppScreen
import com.example.wgmanager.ui.theme.*

// ── Badge data ──────────────────────────────────────────────
data class Badge(
    val id: String,
    val emoji: String,
    val name: String,
    val description: String,
    val unlocked: Boolean
)

private fun getBadges(user: User): List<Badge> {
    val s = AppStrings
    return listOf(
        Badge("early_bird", "🌅", s.badgeEarlyBird, s.badgeEarlyBirdDesc, user.points >= 10),
        Badge("clean_freak", "🧹", s.badgeCleanFreak, s.badgeCleanFreakDesc, user.points >= 50),
        Badge("money_maker", "💰", s.badgeMoneyMaker, s.badgeMoneyMakerDesc, user.points >= 150),
        Badge("party_animal", "🎉", s.badgePartyAnimal, s.badgePartyAnimalDesc, user.points >= 80),
        Badge("top_chef", "👨‍🍳", s.badgeTopChef, s.badgeTopChefDesc, user.points >= 100),
        Badge("ghost", "👻", s.badgeGhost, s.badgeGhostDesc, user.points >= 300),
    )
}

@Composable
fun ProfileScreen(
    onNavigate: (AppScreen) -> Unit,
    toast: ToastState,
    onThemeChange: (ThemeColor) -> Unit,
    onDarkModeChange: (Boolean) -> Unit
) {
    val user = DataStore.currentUser ?: return
    val palette = LocalThemePalette.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showEditProfile by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }
    var showBadgeDetail by remember { mutableStateOf<Badge?>(null) }
    var showEmailDialog by remember { mutableStateOf(false) }
    var showJoinedDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAdminEditChoice by remember { mutableStateOf(false) }
    var showWGSettings by remember { mutableStateOf(false) }
    var showPhotoPermission by remember { mutableStateOf(false) }
    var showJoinRequests by remember { mutableStateOf(false) }
    var showAmenities by remember { mutableStateOf(false) }

    val isAdmin = user.role == UserRole.ADMIN || user.role == UserRole.SUPER_ADMIN
    val s = AppStrings

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // ── Large Gradient Header with Avatar ──
        Box(modifier = Modifier.fillMaxWidth()) {
            // Gradient background with geometric decorations
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Brush.linearGradient(listOf(Color(0xFFE91E63), Color(0xFFD946C8), Color(0xFFAB47BC), Color(0xFF9C27B0))))
                    .drawBehind {
                        val ghost = Color.White.copy(alpha = 0.08f)
                        val ghostStroke = Color.White.copy(alpha = 0.12f)
                        drawRoundRect(color = ghost, topLeft = Offset(60f, 100f), size = Size(40f, 40f), cornerRadius = CornerRadius(6f))
                        val triPath = Path().apply { moveTo(50f, 300f); lineTo(100f, 220f); lineTo(150f, 300f); close() }
                        drawPath(triPath, color = ghost)
                        val triPath2 = Path().apply { moveTo(80f, 180f); lineTo(110f, 130f); lineTo(140f, 180f); close() }
                        drawPath(triPath2, color = ghostStroke, style = Stroke(width = 2f))
                        drawCircle(color = ghostStroke, radius = 50f, center = Offset(size.width - 80f, 200f), style = Stroke(width = 2f))
                        drawCircle(color = ghost, radius = 5f, center = Offset(120f, 140f))
                        drawCircle(color = ghost, radius = 4f, center = Offset(size.width - 150f, 100f))
                        drawRoundRect(color = ghostStroke, topLeft = Offset(size.width / 2 - 30f, 160f), size = Size(25f, 25f), cornerRadius = CornerRadius(4f), style = Stroke(width = 1.5f))
                        drawRoundRect(color = ghostStroke, topLeft = Offset(size.width / 2 + 20f, 180f), size = Size(18f, 18f), cornerRadius = CornerRadius(3f), style = Stroke(width = 1.5f))
                        val triTop = Path().apply { moveTo(size.width / 2 - 20f, 60f); lineTo(size.width / 2, 30f); lineTo(size.width / 2 + 20f, 60f); close() }
                        drawPath(triTop, color = ghostStroke, style = Stroke(width = 1.5f))
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { onNavigate(AppScreen.DASHBOARD) },
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f))
                    ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
                    IconButton(
                        onClick = { if (isAdmin) showAdminEditChoice = true else showEditProfile = true },
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f))
                    ) { Icon(Icons.Default.Edit, "Edit", tint = Color.White) }
                }
            }

            // Avatar overlapping header
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 140.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.background))
                    Surface(
                        modifier = Modifier.size(90.dp).clickable { showEditProfile = true }, 
                        shape = CircleShape, 
                        color = Color(0xFFFF6B9D), 
                        tonalElevation = 4.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) { Text(user.avatarEmoji, fontSize = 40.sp) }
                    }
                    // Camera edit indicator
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-4).dp, y = (-4).dp).size(28.dp).clickable { showEditProfile = true },
                        shape = CircleShape,
                        color = Color(0xFF3B82F6)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                    Box(modifier = Modifier.align(Alignment.BottomCenter).offset(y = 4.dp).size(16.dp).clip(CircleShape).background(WGSuccess).border(2.dp, MaterialTheme.colorScheme.background, CircleShape))
                }
                Spacer(Modifier.height(10.dp))
                Text("${user.name} 👑", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(if (user.bio.isNotBlank()) user.bio else s.defaultBio, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Pill Tab Selector ──
        Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp), shape = RoundedCornerShape(25.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
            Row(modifier = Modifier.padding(3.dp)) {
                listOf(s.overview, s.badges, s.settingsShort).forEachIndexed { idx, label ->
                    val selected = selectedTab == idx
                    Surface(modifier = Modifier.weight(1f).clickable { selectedTab = idx }, shape = RoundedCornerShape(22.dp), color = if (selected) palette.primary else Color.Transparent) {
                        Text(label, modifier = Modifier.padding(vertical = 10.dp), textAlign = TextAlign.Center, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        when (selectedTab) {
            0 -> ProfileOverviewTab(user, palette, Modifier.weight(1f))
            1 -> ProfileBadgesTab(user, Modifier.weight(1f)) { showBadgeDetail = it }
            2 -> ProfileSettingsTab(user, palette, toast, onThemeChange, onDarkModeChange, onChangePassword = { showChangePassword = true }, onEmail = { showEmailDialog = true }, onJoined = { showJoinedDialog = true }, onHelp = { showHelpDialog = true }, onTerms = { showTermsDialog = true }, onPrivacy = { showPrivacyDialog = true }, onTheme = { showThemeDialog = true }, onLogout = { DataStore.logout(); onNavigate(AppScreen.LOGIN) }, onWGSettings = { showWGSettings = true }, onJoinRequests = { showJoinRequests = true }, onAmenities = { showAmenities = true }, modifier = Modifier.weight(1f))
        }

        DashboardBottomNav(current = AppScreen.PROFILE, onNavigate = onNavigate)
    }

    // ── Dialogs ──
    if (showEditProfile) {
        var editName by remember { mutableStateOf(user.name) }
        var editBio by remember { mutableStateOf(user.bio) }
        var editEmoji by remember { mutableStateOf(user.avatarEmoji) }
        val avatarEmojis = listOf(
            "👤", "😊", "😎", "🤓", "🥳", "😴", "🤩", "🥶",
            "👨", "👩", "🧑", "👦", "👧", "🧒", "👨‍🦱", "👩‍🦰",
            "🦊", "🐱", "🐶", "🐻", "🐼", "🐨", "🦁", "🐯",
            "🤖", "👽", "👻", "💀", "🎃", "👾", "🦸", "🧙"
        )
        AlertDialog(
            onDismissRequest = { showEditProfile = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { 
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("✏️ ${s.editMyProfile}", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = { 
                Column {
                    // Avatar preview and selection
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(editEmoji, fontSize = 36.sp)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(s.selectAvatar, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        // Import Photo button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { showPhotoPermission = true }
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("📷", fontSize = 14.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(s.importPhoto, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    
                    // Avatar grid
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            avatarEmojis.chunked(8).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    row.forEach { emoji ->
                                        val isSelected = emoji == editEmoji
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) Color(0xFFEC4899).copy(alpha = 0.3f) else Color.Transparent,
                                            border = if (isSelected) BorderStroke(2.dp, Color(0xFFEC4899)) else null,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clickable { editEmoji = emoji }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(emoji, fontSize = 20.sp)
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = editName, 
                        onValueChange = { editName = it }, 
                        label = { Text(s.nameFieldLabel) }, 
                        shape = RoundedCornerShape(14.dp), 
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEC4899),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = Color(0xFFEC4899),
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editBio, 
                        onValueChange = { editBio = it }, 
                        label = { Text(s.bioFieldLabel) }, 
                        shape = RoundedCornerShape(14.dp), 
                        modifier = Modifier.fillMaxWidth(), 
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEC4899),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = Color(0xFFEC4899),
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                } 
            },
            confirmButton = { 
                Button(
                    onClick = { 
                        user.name = editName
                        user.bio = editBio
                        user.avatarEmoji = editEmoji
                        DataStore.syncUser(user)
                        toast.show(s.profileUpdated)
                        showEditProfile = false 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                    shape = RoundedCornerShape(14.dp)
                ) { Text(s.save, fontWeight = FontWeight.Bold, color = Color.White) } 
            },
            dismissButton = { 
                TextButton(onClick = { showEditProfile = false }) { 
                    Text(s.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant) 
                } 
            }
        )
    }

    if (showChangePassword) {
        var newPw by remember { mutableStateOf("") }
        androidx.compose.ui.window.Dialog(onDismissRequest = { showChangePassword = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(s.changePassword, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = { showChangePassword = false },
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Close, s.close, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }
                    
                    Spacer(Modifier.height(20.dp))
                    
                    OutlinedTextField(
                        value = newPw,
                        onValueChange = { newPw = it },
                        placeholder = { Text(s.newPassword, color = MaterialTheme.colorScheme.outline) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedBorderColor = Color(0xFF6366F1),
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = Color(0xFF6366F1)
                        )
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            if (newPw.isNotBlank()) {
                                user.password = newPw
                                DataStore.syncUser(user)
                                toast.show(s.passwordChangedFull)
                                showChangePassword = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                    ) {
                        Text(s.save, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }

    showBadgeDetail?.let { badge ->
        InfoDialog(badge.name, onDismiss = { showBadgeDetail = null }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(8.dp))
                Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = if (badge.unlocked) palette.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant) {
                    Box(contentAlignment = Alignment.Center) { Text(badge.emoji, fontSize = 36.sp) }
                }
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(20.dp), color = if (badge.unlocked) WGSuccess.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant) {
                    Text(if (badge.unlocked) AppStrings.unlockedUpper else AppStrings.lockedUpper, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), color = if (badge.unlocked) WGSuccess else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.height(16.dp))
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                    Text("\"${badge.description}\"", modifier = Modifier.padding(16.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (showEmailDialog) { InfoDialog(s.email, onDismiss = { showEmailDialog = false }) {
        Text(s.emailDescription, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) { Text(user.email, modifier = Modifier.fillMaxWidth().padding(14.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Medium) }
    } }
    if (showJoinedDialog) { InfoDialog(s.memberSince, onDismiss = { showJoinedDialog = false }) { Text(s.memberSinceDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    if (showHelpDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showHelpDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(s.help, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = { showHelpDialog = false },
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Close, s.close, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        s.helpText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    
                    Spacer(Modifier.height(20.dp))
                    
                    OutlinedButton(
                        onClick = { showHelpDialog = false },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(s.close, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
    if (showTermsDialog) { InfoDialog(s.termsLabel, onDismiss = { showTermsDialog = false }) { Text(s.termsContent, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    if (showPrivacyDialog) { InfoDialog(s.privacyLabel, onDismiss = { showPrivacyDialog = false }) { Text(s.privacyContent, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }

    // Theme Picker Dialog matching screenshot
    if (showThemeDialog) {
        val themes = listOf(
            Triple(s.neonNight, listOf(Color(0xFF9333EA), Color(0xFFEC4899)), ThemeColor.INDIGO),
            Triple(s.freshMint, listOf(Color(0xFF10B981), Color(0xFF34D399)), ThemeColor.EMERALD),
            Triple(s.sweetCandy, listOf(Color(0xFFEC4899), Color(0xFFF472B6)), ThemeColor.ROSE),
            Triple(s.sunset, listOf(Color(0xFFF97316), Color(0xFFFBBF24)), ThemeColor.AMBER),
            Triple(s.ocean, listOf(Color(0xFF0EA5E9), Color(0xFF38BDF8)), ThemeColor.SKY)
        )
        var selectedTheme by remember { mutableStateOf(user.themeColor) }
        
        androidx.compose.ui.window.Dialog(onDismissRequest = { showThemeDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(s.chooseDesign, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = { showThemeDialog = false },
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Close, s.close, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }
                    
                    Spacer(Modifier.height(20.dp))
                    
                    themes.forEach { (name, colors, themeColor) ->
                        val isSelected = selectedTheme == themeColor
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.background,
                            border = if (isSelected) BorderStroke(2.dp, Color(0xFF6366F1)) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { selectedTheme = themeColor }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Gradient circle
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(colors))
                                )
                                Spacer(Modifier.width(14.dp))
                                Text(name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFF6366F1)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    TextButton(
                        onClick = {
                            user.themeColor = selectedTheme
                            DataStore.syncUser(user)
                            onThemeChange(selectedTheme)
                            toast.show("${s.designChanged} 🎨")
                            showThemeDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(s.done, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }

    // Admin edit choice dialog
    if (showAdminEditChoice) {
        val s = AppStrings
        AlertDialog(
            onDismissRequest = { showAdminEditChoice = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("✏️ ${s.whatToEdit}", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Edit my profile
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAdminEditChoice = false
                                showEditProfile = true
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEC4899).copy(alpha = 0.2f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("👤", fontSize = 22.sp)
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(s.editMyProfile, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(user.name, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
                        }
                    }

                    // WG Einstellungen (combined)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAdminEditChoice = false
                                showWGSettings = true
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF3B82F6).copy(alpha = 0.2f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🏠", fontSize = 22.sp)
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(s.wgEinstellungen, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(DataStore.currentWG?.name ?: "WG", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAdminEditChoice = false }) {
                    Text(AppStrings.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // ═══════════════════════════════════════════════════════════
    // WG Infos bearbeiten (combined admin dialog)
    // ═══════════════════════════════════════════════════════════
    if (showWGSettings) {
        val s = AppStrings
        val currentWG = DataStore.currentWG
        var editRent by remember { mutableStateOf((currentWG?.rentPrice ?: 0).toString()) }
        var editDesc by remember { mutableStateOf(currentWG?.publicDescription ?: "") }
        var editRules by remember { mutableStateOf(currentWG?.wgRules ?: "") }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showWGSettings = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🏠 ${s.wgInfosBearbeiten}",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showWGSettings = false },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Close, s.close, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── MONATLICHE MIETE (€) ──
                    Text(
                        s.monatlicheMiete,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editRent,
                        onValueChange = { editRent = it.filter { c -> c.isDigit() } },
                        placeholder = { Text("450", color = MaterialTheme.colorScheme.outline) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = { Text("€", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = Color(0xFF3B82F6)
                        )
                    )

                    Spacer(Modifier.height(20.dp))

                    // ── ÖFFENTLICHE BESCHREIBUNG ──
                    Text(
                        s.oeffentlicheBeschreibung,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        placeholder = { Text(s.publicDescription, color = MaterialTheme.colorScheme.outline) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = Color(0xFF3B82F6)
                        )
                    )

                    Spacer(Modifier.height(20.dp))

                    // ── HAUSREGELN (FÜR ONBOARDING) ──
                    Text(
                        s.hausregelnFuerOnboarding,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editRules,
                        onValueChange = { editRules = it },
                        placeholder = { Text(s.wgRulesHint, color = MaterialTheme.colorScheme.outline) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 5,
                        maxLines = 8,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = Color(0xFF3B82F6)
                        )
                    )

                    Spacer(Modifier.height(28.dp))

                    // ── SPEICHERN ──
                    Button(
                        onClick = {
                            val rent = editRent.toIntOrNull() ?: 0
                            DataStore.updateWGShowcase(rent, editDesc)
                            DataStore.updateWGRules(editRules)
                            toast.show("${s.wgInfosUpdated} 🏠")
                            showWGSettings = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        Text(s.save, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // Join Requests dialog (Admin)
    // ═══════════════════════════════════════════════════════════
    if (showJoinRequests) {
        val s = AppStrings
        var requests by remember { mutableStateOf(DataStore.getPendingRequestsForMyWG()) }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showJoinRequests = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .heightIn(max = 500.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "📬 ${s.joinRequests}",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showJoinRequests = false },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Close, s.close, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    if (requests.isEmpty()) {
                        // Empty state
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📭", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                s.noJoinRequests,
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            requests.forEach { request ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.background
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFF3B82F6).copy(alpha = 0.2f),
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                    Text("👤", fontSize = 20.sp)
                                                }
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    request.userName,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                )
                                                Text(
                                                    request.userEmail,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                        if (request.message.isNotBlank()) {
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                "\"${request.message}\"",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 13.sp,
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                            )
                                        }
                                        Spacer(Modifier.height(12.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = {
                                                    DataStore.acceptJoinRequest(request)
                                                    requests = DataStore.getPendingRequestsForMyWG()
                                                    toast.show("${s.requestAcceptedMsg} ✅")
                                                },
                                                modifier = Modifier.weight(1f).height(40.dp),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                                            ) {
                                                Text(s.acceptBtn, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    DataStore.rejectJoinRequest(request)
                                                    requests = DataStore.getPendingRequestsForMyWG()
                                                    toast.show(s.requestRejectedMsg)
                                                },
                                                modifier = Modifier.weight(1f).height(40.dp),
                                                shape = RoundedCornerShape(10.dp),
                                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                                            ) {
                                                Text(s.rejectBtn, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    // Amenities management dialog (Admin)
    // ═══════════════════════════════════════════════════════════
    if (showAmenities) {
        val s = AppStrings
        val allAmenities = DataStore.getAvailableAmenities()
        var activeAmenities by remember { mutableStateOf(DataStore.currentWG?.amenities?.toList() ?: emptyList()) }

        fun amenityLabel(key: String): String = when (key) {
            "wifi" -> s.amenityWifi; "washer" -> s.amenityWasher; "dryer" -> s.amenityDryer
            "parking" -> s.amenityParking; "balcony" -> s.amenityBalcony; "garden" -> s.amenityGarden
            "dishwasher" -> s.amenityDishwasher; "elevator" -> s.amenityElevator
            "bike_storage" -> s.amenityBikeStorage; "cellar" -> s.amenityCellar
            "bathtub" -> s.amenityBathtub; "tv" -> s.amenityTv; else -> key
        }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showAmenities = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🏠 ${s.amenitiesManage}",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showAmenities = false },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Close, s.close, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Amenity grid
                    val columns = 3
                    val rows = (allAmenities.size + columns - 1) / columns
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        repeat(rows) { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                repeat(columns) { col ->
                                    val idx = row * columns + col
                                    if (idx < allAmenities.size) {
                                        val (key, emoji) = allAmenities[idx]
                                        val isActive = activeAmenities.contains(key)
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = if (isActive) Color(0xFF3B82F6).copy(alpha = 0.2f) else MaterialTheme.colorScheme.background,
                                            border = BorderStroke(
                                                1.dp,
                                                if (isActive) Color(0xFF3B82F6) else MaterialTheme.colorScheme.outline
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    if (isActive) {
                                                        DataStore.removeAmenity(key)
                                                    } else {
                                                        DataStore.addAmenity(key)
                                                    }
                                                    activeAmenities = DataStore.currentWG?.amenities?.toList() ?: emptyList()
                                                }
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(emoji, fontSize = 24.sp)
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    amenityLabel(key),
                                                    color = if (isActive) Color(0xFF3B82F6) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1
                                                )
                                                if (isActive) {
                                                    Spacer(Modifier.height(2.dp))
                                                    Icon(
                                                        Icons.Default.CheckCircle,
                                                        null,
                                                        tint = Color(0xFF3B82F6),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            toast.show("${s.amenitiesUpdated} 🏠")
                            showAmenities = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        Text(s.save, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }

    // Photo upload permission request dialog
    if (showPhotoPermission) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showPhotoPermission = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("📸", fontSize = 28.sp)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(s.accessRequest, color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(s.accessRequestText, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(20.dp))
                    
                    // Permission items
                    listOf(
                        Triple("📷", s.cameraPermission, ""),
                        Triple("🖼️", s.galleryPermission, ""),
                        Triple("💾", s.storagePermission, "")
                    ).forEach { (emoji, label, _) ->
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(emoji, fontSize = 20.sp)
                                Spacer(Modifier.width(12.dp))
                                Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF22C55E), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            showPhotoPermission = false
                            toast.show("${s.importPhoto} 📸")
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                    ) {
                        Text(s.allowAccess, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { showPhotoPermission = false }) {
                        Text(s.denyAccess, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoDialog(title: String, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, shape = RoundedCornerShape(20.dp),
        title = { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Close, AppStrings.close, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        } },
        text = { Column { content() } },
        confirmButton = { OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text(AppStrings.close, fontWeight = FontWeight.Bold) } })
}

// ═══════════════════════════════════════════════════════════════
// TAB 0: OVERVIEW
// ═══════════════════════════════════════════════════════════════
@Composable
private fun ProfileOverviewTab(user: User, palette: ThemePalette, modifier: Modifier = Modifier) {
    val s = AppStrings
    Column(modifier = modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        // Rank Card (gradient)
        Box(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFFE91E63), Color(0xFFD946C8), Color(0xFFAB47BC))), RoundedCornerShape(20.dp)).padding(20.dp)) {
            Column {
                Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.2f)) {
                    Text(" ${s.currentRank} ", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val rankName = user.levelTitle.replace(Regex("^[^ ]+ "), "")
                    Text(rankName, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${user.points}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 32.sp)
                        Text("/ 1000 XP", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.2f))) {
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(user.points / 1000f).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.7f)))
                }
                Spacer(Modifier.height(6.dp))
                val xpNeeded = 1000 - user.points
                Text("${s.xpToNextLevel} $xpNeeded ${s.xpToNextLevelEnd}", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
            }
            Text("🏆", fontSize = 32.sp, modifier = Modifier.align(Alignment.TopEnd))
        }

        Spacer(Modifier.height(16.dp))
        Text(s.thisMonth, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
        Spacer(Modifier.height(8.dp))

        val tasksDone = DataStore.tasks.count { it.assignedTo == user.name && it.completed }
        val totalSpent = DataStore.shoppingItems.filter { it.boughtBy == user.name && it.status == ShoppingStatus.BOUGHT }.sumOf { it.price }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ProfileStatCard("✅", palette.primary, "$tasksDone", s.completed, s.top10Pct, palette.primary, Modifier.weight(1f))
            ProfileStatCard("💰", WGWarning, "${totalSpent.toInt()}€", s.spending, s.avgLabel, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ProfileStatCard("✨", Color(0xFFAB47BC), "5 ${s.daysUnit}", s.streak, s.onFire, WGDanger, Modifier.weight(1f))
            ProfileStatCard("⚡", WGWarning, s.fastLabel, s.speed, s.approxMins, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f))
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun ProfileStatCard(icon: String, iconColor: Color, value: String, label: String, badge: String?, badgeColor: Color, modifier: Modifier = Modifier) {
    WGCard(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = iconColor.copy(alpha = 0.12f), modifier = Modifier.size(36.dp)) { Box(contentAlignment = Alignment.Center) { Text(icon, fontSize = 16.sp) } }
                Spacer(Modifier.weight(1f))
                badge?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = badgeColor, fontWeight = FontWeight.SemiBold) }
            }
            Spacer(Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// TAB 1: BADGES
// ═══════════════════════════════════════════════════════════════
@Composable
private fun ProfileBadgesTab(user: User, modifier: Modifier = Modifier, onBadgeClick: (Badge) -> Unit) {
    val badges = remember { getBadges(user) }
    LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
        items(badges) { badge ->
            WGCard(onClick = { onBadgeClick(badge) }, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(badge.emoji, fontSize = 32.sp, modifier = if (!badge.unlocked) Modifier.graphicsLayer(alpha = 0.35f) else Modifier)
                    Spacer(Modifier.height(6.dp))
                    Text(badge.name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1, modifier = if (!badge.unlocked) Modifier.graphicsLayer(alpha = 0.5f) else Modifier)
                    Spacer(Modifier.height(4.dp))
                    Surface(shape = RoundedCornerShape(10.dp), color = if (badge.unlocked) WGSuccess.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant) {
                        Text(if (badge.unlocked) AppStrings.unlocked else AppStrings.locked, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (badge.unlocked) WGSuccess else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// TAB 2: SETTINGS
// ═══════════════════════════════════════════════════════════════
@Composable
private fun ProfileSettingsTab(
    user: User, palette: ThemePalette, toast: ToastState,
    onThemeChange: (ThemeColor) -> Unit, onDarkModeChange: (Boolean) -> Unit,
    onChangePassword: () -> Unit, onEmail: () -> Unit, onJoined: () -> Unit,
    onHelp: () -> Unit, onTerms: () -> Unit, onPrivacy: () -> Unit,
    onTheme: () -> Unit, onLogout: () -> Unit, onWGSettings: () -> Unit,
    onJoinRequests: () -> Unit, onAmenities: () -> Unit,
    modifier: Modifier = Modifier
) {
    var notifications by remember { mutableStateOf(user.notificationsEnabled) }
    var darkMode by remember { mutableStateOf(user.isDarkMode) }
    var twoFA by remember { mutableStateOf(user.isTwoFactorEnabled) }
    var language by remember { mutableStateOf(user.language) }
    val themeName = when(user.themeColor) { ThemeColor.INDIGO -> "Indigo"; ThemeColor.EMERALD -> "Emerald"; ThemeColor.ROSE -> "Rose"; ThemeColor.AMBER -> "Amber"; ThemeColor.SKY -> "Sky" }
    val s = AppStrings

    Column(modifier = modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        SettingsGroupHeader(s.settingsUpper)
        WGCard(modifier = Modifier.fillMaxWidth()) { Column {
            SettingsRow("🌐", s.language, trailing = { Text(if (language == "DE") s.german else s.english, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall); Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }, onClick = { language = if (language == "DE") "EN" else "DE"; user.language = language; DataStore.syncUser(user); toast.show("${s.language}: $language") })
            SettingsDivider()
            SettingsRow("🔔", s.notifications, trailing = { Switch(checked = notifications, onCheckedChange = { notifications = it; user.notificationsEnabled = it; DataStore.syncUser(user) }, colors = SwitchDefaults.colors(checkedTrackColor = palette.primary)) })
            SettingsDivider()
            SettingsRow("🌙", s.darkMode, trailing = { Switch(checked = darkMode, onCheckedChange = { darkMode = it; user.isDarkMode = it; DataStore.syncUser(user); onDarkModeChange(it) }, colors = SwitchDefaults.colors(checkedTrackColor = palette.primary)) })
            SettingsDivider()
            SettingsRow("🎨", s.appDesign, trailing = { Text(themeName, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall); Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }, onClick = onTheme)
        } }

        Spacer(Modifier.height(16.dp))
        SettingsGroupHeader(s.securityUpper)
        WGCard(modifier = Modifier.fillMaxWidth()) { Column {
            SettingsRow("🛡️", s.twoFA, trailing = { Text(if (twoFA) "On" else "Off", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall); Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }, onClick = { twoFA = !twoFA; user.isTwoFactorEnabled = twoFA; DataStore.syncUser(user); toast.show(if (twoFA) s.twoFaActivated else s.twoFaDeactivated) })
            SettingsDivider()
            SettingsRow("🔒", s.changePassword, trailing = { Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }, onClick = onChangePassword)
        } }

        Spacer(Modifier.height(16.dp))
        SettingsGroupHeader(s.legalUpper)
        WGCard(modifier = Modifier.fillMaxWidth()) { Column {
            SettingsRow("❓", s.helpCenter, trailing = { Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }, onClick = onHelp)
            SettingsDivider()
            SettingsRow("📜", s.termsLabel, trailing = { Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }, onClick = onTerms)
            SettingsDivider()
            SettingsRow("🔐", s.privacyLabel, trailing = { Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }, onClick = onPrivacy)
        } }

        Spacer(Modifier.height(16.dp))
        SettingsGroupHeader(s.accountUpper)
        WGCard(modifier = Modifier.fillMaxWidth()) { Column {
            SettingsRow("✉️", s.email, trailing = { Text(user.email, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall); Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }, onClick = onEmail)
            SettingsDivider()
            SettingsRow("📅", s.joinedOn, trailing = { Text("Oct 2023", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall); Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) }, onClick = onJoined)
        } }

        // ── ADMIN ZONE ──
        val isAdmin = user.role == UserRole.ADMIN || user.role == UserRole.SUPER_ADMIN
        if (isAdmin) {
            val pendingCount = DataStore.getPendingRequestsForMyWG().size
            Spacer(Modifier.height(16.dp))
            SettingsGroupHeader(AppStrings.adminZone)
            WGCard(modifier = Modifier.fillMaxWidth()) { Column {
                SettingsRow("⚙️", AppStrings.wgEinstellungen, trailing = {
                    Text(DataStore.currentWG?.name ?: "WG", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }, onClick = onWGSettings)
                SettingsDivider()
                SettingsRow("📬", AppStrings.joinRequests, trailing = {
                    if (pendingCount > 0) {
                        Surface(shape = CircleShape, color = Color(0xFFEF4444), modifier = Modifier.size(22.dp)) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text("$pendingCount", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }, onClick = onJoinRequests)
                SettingsDivider()
                SettingsRow("🏠", AppStrings.amenitiesManage, trailing = {
                    Text("${DataStore.currentWG?.amenities?.size ?: 0}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }, onClick = onAmenities)
            } }
        }

        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = WGDanger), border = BorderStroke(1.dp, WGDanger.copy(alpha = 0.3f))) {
            Text(s.signOutBtn, fontWeight = FontWeight.Bold, color = WGDanger)
        }
        Spacer(Modifier.height(8.dp))
        Text(s.appVersion, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsGroupHeader(title: String) {
    Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun SettingsRow(icon: String, label: String, trailing: @Composable RowScope.() -> Unit = {}, onClick: (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(horizontal = 14.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(36.dp)) { Box(contentAlignment = Alignment.Center) { Text(icon, fontSize = 16.sp) } }
        Spacer(Modifier.width(12.dp))
        Text(label, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) { trailing() }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}
