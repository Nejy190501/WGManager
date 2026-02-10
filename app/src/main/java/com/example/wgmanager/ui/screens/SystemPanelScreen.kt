package com.example.wgmanager.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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

// ═══════════════════════════════════════════════════════════════
// SYSTEM PANEL V2 — Super Admin God Mode Cockpit
// ═══════════════════════════════════════════════════════════════

// Dark theme colors for the panel
private val PanelBg = Color(0xFF0B1120)
private val PanelSurface = Color(0xFF131B2E)
private val PanelSurfaceLight = Color(0xFF1A2340)
private val PanelBorder = Color(0xFF2A3555)
private val PanelText = Color(0xFFE2E8F0)
private val PanelTextDim = Color(0xFF94A3B8)
private val PanelAccent = Color(0xFF8B5CF6)
private val PanelAccentLight = Color(0xFFA78BFA)
private val PanelPink = Color(0xFFEC4899)
private val PanelGreen = Color(0xFF10B981)
private val PanelRed = Color(0xFFEF4444)
private val PanelAmber = Color(0xFFF59E0B)
private val PanelBlue = Color(0xFF3B82F6)

@Composable
fun SystemPanelScreen(onNavigate: (AppScreen) -> Unit, toast: ToastState) {
    val user = DataStore.currentUser ?: return
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Dashboard, 1=Users, 2=WGs
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var showNukeDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteWGDialog by remember { mutableStateOf<WG?>(null) }
    var showManageMembersDialog by remember { mutableStateOf<WG?>(null) }

    // Initialize system logs
    LaunchedEffect(Unit) {
        if (DataStore.systemLogs.isEmpty()) {
            DataStore.addSystemLog(AppStrings.spAdminSession)
            DataStore.addSystemLog(AppStrings.spConnectedDb)
            DataStore.addSystemLog(AppStrings.spSystemInit)
        }
    }

    // Console log state — track changes
    var logVersion by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PanelBg)
    ) {
        // ── HEADER — purple gradient banner ──────────────────
        PanelHeader(
            userName = user.name,
            avatarEmoji = user.avatarEmoji,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        // ── CONTENT ──────────────────────────────────────────
        when (selectedTab) {
            0 -> DashboardTab(
                onMaintenanceToggle = {
                    DataStore.toggleMaintenanceMode()
                    logVersion++
                    toast.show(
                        if (DataStore.maintenanceMode) AppStrings.spMaintenanceEnabled
                        else AppStrings.spMaintenanceDisabled
                    )
                },
                onBroadcast = { showBroadcastDialog = true },
                onNuke = { showNukeDialog = true },
                maintenanceMode = DataStore.maintenanceMode,
                logVersion = logVersion,
                onTerminate = {
                    DataStore.addSystemLog("↩️ Session terminated by ${user.name}")
                    DataStore.logout()
                    onNavigate(AppScreen.LOGIN)
                }
            )
            1 -> UsersTab(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onLoginAs = { targetUser ->
                    DataStore.impersonateUser(targetUser)
                    logVersion++
                    toast.show("${AppStrings.spImpersonating} ${targetUser.name}")
                    // Navigate to what the user would see
                    when {
                        !targetUser.hasWG -> onNavigate(AppScreen.WG_FINDER)
                        !targetUser.onboardingCompleted -> onNavigate(AppScreen.ONBOARDING)
                        else -> onNavigate(AppScreen.DASHBOARD)
                    }
                },
                onMakeAdmin = { targetUser ->
                    if (targetUser.role == UserRole.ADMIN) {
                        DataStore.demoteToUser(targetUser)
                        logVersion++
                        toast.show(AppStrings.spUserDemoted)
                    } else {
                        DataStore.promoteToAdmin(targetUser)
                        logVersion++
                        toast.show(AppStrings.spUserPromoted)
                    }
                },
                onBan = { targetUser ->
                    if (targetUser.isBanned) {
                        DataStore.unbanUser(targetUser)
                        logVersion++
                        toast.show(AppStrings.spUserUnbanned)
                    } else {
                        DataStore.banUser(targetUser)
                        logVersion++
                        toast.show(AppStrings.spUserBanned)
                    }
                },
                onTerminate = {
                    DataStore.addSystemLog("↩️ Session terminated by ${user.name}")
                    DataStore.logout()
                    onNavigate(AppScreen.LOGIN)
                }
            )
            2 -> WGsTab(
                onDeleteWG = { wg -> showDeleteWGDialog = wg },
                onManageMembers = { wg -> showManageMembersDialog = wg },
                onTerminate = {
                    DataStore.addSystemLog("↩️ Session terminated by ${user.name}")
                    DataStore.logout()
                    onNavigate(AppScreen.LOGIN)
                }
            )
        }
    }

    // ── DELETE WG CONFIRM DIALOG ─────────────────────────────
    showDeleteWGDialog?.let { wg ->
        DeleteWGConfirmDialog(
            wgName = wg.name,
            memberCount = DataStore.getWGMembers(wg.id).size,
            onDismiss = { showDeleteWGDialog = null },
            onConfirm = {
                DataStore.deleteWG(wg)
                showDeleteWGDialog = null
                toast.show(AppStrings.spWgDeleted)
            }
        )
    }

    // ── MANAGE MEMBERS DIALOG ────────────────────────────────
    showManageMembersDialog?.let { wg ->
        ManageMembersDialog(
            wg = wg,
            onDismiss = { showManageMembersDialog = null },
            toast = toast
        )
    }

    // ── BROADCAST DIALOG ─────────────────────────────────────
    if (showBroadcastDialog) {
        BroadcastDialog(
            onDismiss = { showBroadcastDialog = false },
            onSend = { message ->
                DataStore.sendBroadcast(message)
                logVersion++
                showBroadcastDialog = false
                toast.show(AppStrings.spBroadcastSent)
            }
        )
    }

    // ── NUKE CONFIRMATION DIALOG ─────────────────────────────
    if (showNukeDialog) {
        NukeConfirmDialog(
            onDismiss = { showNukeDialog = false },
            onConfirm = {
                DataStore.nukeDatabase()
                logVersion++
                showNukeDialog = false
                toast.show(AppStrings.spNuked)
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// PANEL HEADER — Purple gradient with avatar + tab toggle
// ═══════════════════════════════════════════════════════════════
@Composable
private fun PanelHeader(
    userName: String,
    avatarEmoji: String,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF6D28D9),
                        Color(0xFF9333EA),
                        Color(0xFFDB2777)
                    )
                )
            )
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = (-40).dp, y = (-30).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = 300.dp, y = 20.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp)
        ) {
            // Terminal label
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(">_", color = Color.White.copy(alpha = 0.7f), fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text(
                    AppStrings.systemPanelV2,
                    color = Color.White.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(12.dp))

            // Avatar + Name + Tab Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with rocket
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🚀", fontSize = 28.sp)
                }

                Spacer(Modifier.width(12.dp))

                // Name + God Mode badge
                Column {
                    Text(
                        userName,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = PanelGreen
                    ) {
                        Text(
                            AppStrings.godMode,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // Tab Toggle — Dashboard / Users / WGs
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        listOf(AppStrings.spDashboard, AppStrings.spUsers, AppStrings.spWgs).forEachIndexed { idx, label ->
                            val isSelected = selectedTab == idx
                            val bgColor by animateColorAsState(
                                if (isSelected) Color.White else Color.Transparent,
                                label = "tabBg"
                            )
                            val textColor by animateColorAsState(
                                if (isSelected) Color(0xFF1E1B4B) else Color.White.copy(alpha = 0.8f),
                                label = "tabText"
                            )
                            Surface(
                                onClick = { onTabSelected(idx) },
                                shape = RoundedCornerShape(8.dp),
                                color = bgColor,
                                modifier = Modifier.height(30.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 14.dp)
                                ) {
                                    Text(
                                        label,
                                        color = textColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// DASHBOARD TAB — KPIs, System Control, Console
// ═══════════════════════════════════════════════════════════════
@Composable
private fun DashboardTab(
    onMaintenanceToggle: () -> Unit,
    onBroadcast: () -> Unit,
    onNuke: () -> Unit,
    maintenanceMode: Boolean,
    logVersion: Int,
    onTerminate: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── KPI CARDS ────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    icon = Icons.Default.Group,
                    value = "${DataStore.users.size}",
                    label = AppStrings.spUsersCount,
                    iconColor = PanelBlue,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    icon = Icons.Default.Home,
                    value = "${DataStore.wgs.size}",
                    label = AppStrings.spWgsCount,
                    iconColor = PanelAccent,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    icon = Icons.Default.TrendingUp,
                    value = DataStore.getSystemUptime(),
                    label = AppStrings.spUptime,
                    iconColor = PanelGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── SYSTEM CONTROL HEADER ────────────────────────────
        item {
            Text(
                AppStrings.spSystemControl,
                color = PanelTextDim,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // ── MAINTENANCE MODE TOGGLE ──────────────────────────
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PanelSurface,
                border = BorderStroke(1.dp, PanelBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PanelSurfaceLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = PanelTextDim,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            AppStrings.spMaintenanceMode,
                            color = PanelText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            AppStrings.spMaintenanceDesc,
                            color = PanelTextDim,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = maintenanceMode,
                        onCheckedChange = { onMaintenanceToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PanelGreen,
                            uncheckedThumbColor = PanelTextDim,
                            uncheckedTrackColor = PanelSurfaceLight
                        )
                    )
                }
            }
        }

        // ── BROADCAST ALERT BUTTON ───────────────────────────
        item {
            Surface(
                onClick = onBroadcast,
                shape = RoundedCornerShape(14.dp),
                color = PanelSurface,
                border = BorderStroke(1.dp, PanelBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PanelAmber.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📢", fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(
                        AppStrings.spBroadcastAlert,
                        color = PanelText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // ── NUKE DATABASE BUTTON ─────────────────────────────
        item {
            Surface(
                onClick = onNuke,
                shape = RoundedCornerShape(14.dp),
                color = PanelSurface,
                border = BorderStroke(1.dp, PanelBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PanelRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🗑️", fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(
                        AppStrings.spNukeDatabase,
                        color = PanelRed,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // ── LIVE CONSOLE ─────────────────────────────────────
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(Icons.Default.Terminal, contentDescription = null, tint = PanelTextDim, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    AppStrings.spLiveConsole,
                    color = PanelTextDim,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }
        }

        item {
            // Force recomposition on logVersion
            @Suppress("UNUSED_EXPRESSION")
            logVersion
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0D1117),
                border = BorderStroke(1.dp, PanelBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .heightIn(min = 120.dp, max = 280.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    val logs = DataStore.systemLogs.ifEmpty {
                        listOf("System initialized...")
                    }
                    logs.forEach { log ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text(
                                "> ",
                                color = PanelGreen.copy(alpha = 0.6f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                            Text(
                                log,
                                color = PanelTextDim,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                    // Blinking cursor
                    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
                    val cursorAlpha by infiniteTransition.animateFloat(
                        initialValue = 1f, targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "cursorBlink"
                    )
                    Text(
                        "_ ",
                        color = PanelGreen.copy(alpha = cursorAlpha),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // ── TERMINATE SESSION ────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(onClick = onTerminate) {
                    Text("↪ ", color = PanelRed, fontSize = 16.sp)
                    Text(
                        AppStrings.spTerminateSession,
                        color = PanelRed,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// USERS TAB — Search, User cards with actions
// ═══════════════════════════════════════════════════════════════
@Composable
private fun UsersTab(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onLoginAs: (User) -> Unit,
    onMakeAdmin: (User) -> Unit,
    onBan: (User) -> Unit,
    onTerminate: () -> Unit
) {
    val allUsers = DataStore.users
    val filteredUsers = remember(searchQuery, allUsers.size) {
        if (searchQuery.isBlank()) allUsers.toList()
        else allUsers.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.email.contains(searchQuery, ignoreCase = true)
        }
    }

    // Force recomposition on user changes
    var refreshKey by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── SEARCH BAR ───────────────────────────────────────
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PanelSurface,
                border = BorderStroke(1.dp, PanelBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = PanelTextDim,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = {
                            Text(
                                AppStrings.spSearchPlaceholder,
                                color = PanelTextDim,
                                fontSize = 14.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedTextColor = PanelText,
                            unfocusedTextColor = PanelText,
                            cursorColor = PanelAccent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                    )
                }
            }
        }

        // ── USER CARDS ───────────────────────────────────────
        @Suppress("UNUSED_EXPRESSION")
        refreshKey
        items(filteredUsers, key = { "${it.id}_${it.role.name}_${it.isBanned}_$refreshKey" }) { u ->
            UserAdminCard(
                user = u,
                onLoginAs = {
                    onLoginAs(u)
                },
                onMakeAdmin = {
                    onMakeAdmin(u)
                    refreshKey++
                },
                onBan = {
                    onBan(u)
                    refreshKey++
                }
            )
        }

        // ── TERMINATE SESSION ────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(onClick = onTerminate) {
                    Text("↪ ", color = PanelRed, fontSize = 16.sp)
                    Text(
                        AppStrings.spTerminateSession,
                        color = PanelRed,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// USER ADMIN CARD — Matches screenshot exactly
// ═══════════════════════════════════════════════════════════════
@Composable
private fun UserAdminCard(
    user: User,
    onLoginAs: () -> Unit,
    onMakeAdmin: () -> Unit,
    onBan: () -> Unit
) {
    val roleBadgeText = when {
        user.isBanned -> AppStrings.spBanned
        user.role == UserRole.SUPER_ADMIN -> AppStrings.spSuperAdmin
        user.role == UserRole.ADMIN -> AppStrings.spAdmin
        else -> AppStrings.spUser
    }
    val roleBadgeColor = when {
        user.isBanned -> PanelRed
        user.role == UserRole.SUPER_ADMIN -> PanelPink
        user.role == UserRole.ADMIN -> PanelAccent
        else -> PanelTextDim
    }
    val initials = user.name.take(2).uppercase()

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = PanelSurface,
        border = BorderStroke(1.dp, PanelBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Avatar + Name/Email + XP
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Initials Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PanelSurfaceLight)
                        .border(1.dp, PanelBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        initials,
                        color = PanelTextDim,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Name + Role badge + Email
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            user.name,
                            color = PanelText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = roleBadgeColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, roleBadgeColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                roleBadgeText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                color = roleBadgeColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    Text(
                        user.email,
                        color = PanelTextDim,
                        fontSize = 12.sp
                    )
                }

                // XP Points
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${user.points} ${AppStrings.spXp}",
                        color = PanelAccentLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        AppStrings.spPoints,
                        color = PanelTextDim,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Login As
                Surface(
                    onClick = onLoginAs,
                    shape = RoundedCornerShape(8.dp),
                    color = PanelSurfaceLight,
                    border = BorderStroke(1.dp, PanelBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = null,
                            tint = PanelTextDim,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            AppStrings.spLoginAs,
                            color = PanelText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Make Admin / Demote
                Surface(
                    onClick = onMakeAdmin,
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.SupervisorAccount,
                            contentDescription = null,
                            tint = PanelBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (user.role == UserRole.ADMIN) AppStrings.spDemote else AppStrings.spMakeAdmin,
                            color = PanelBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // Ban / Unban
                Surface(
                    onClick = onBan,
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (user.isBanned) Icons.Default.LockOpen else Icons.Default.Block,
                            contentDescription = null,
                            tint = PanelRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (user.isBanned) AppStrings.spUnban else AppStrings.spBan,
                            color = PanelRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// KPI CARD
// ═══════════════════════════════════════════════════════════════
@Composable
private fun KpiCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = PanelSurface,
        border = BorderStroke(1.dp, PanelBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                color = PanelText,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Text(
                label,
                color = PanelTextDim,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// BROADCAST DIALOG
// ═══════════════════════════════════════════════════════════════
@Composable
private fun BroadcastDialog(
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var message by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PanelSurface,
            border = BorderStroke(1.dp, PanelBorder)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    AppStrings.spBroadcastTitle,
                    color = PanelText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = { Text(AppStrings.spBroadcastHint, color = PanelTextDim) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PanelText,
                        unfocusedTextColor = PanelText,
                        cursorColor = PanelAccent,
                        focusedBorderColor = PanelAccent,
                        unfocusedBorderColor = PanelBorder
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(AppStrings.cancel, color = PanelTextDim)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { if (message.isNotBlank()) onSend(message) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PanelAmber),
                        enabled = message.isNotBlank()
                    ) {
                        Text("📢 Send", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// NUKE CONFIRMATION DIALOG
// ═══════════════════════════════════════════════════════════════
@Composable
private fun NukeConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    // Pulsing animation for the warning icon
    val infiniteTransition = rememberInfiniteTransition(label = "nuke")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "nukeScale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PanelSurface,
            border = BorderStroke(2.dp, PanelRed.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "☢️",
                    fontSize = 48.sp,
                    modifier = Modifier.scale(scale)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    AppStrings.spNukeConfirmTitle,
                    color = PanelRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    AppStrings.spConfirmNuke,
                    color = PanelTextDim,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )
                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, PanelBorder)
                    ) {
                        Text(AppStrings.cancel, color = PanelTextDim)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PanelRed)
                    ) {
                        Text("🔥 Nuke", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// WGS TAB — All WGs with management actions
// ═══════════════════════════════════════════════════════════════
@Composable
private fun WGsTab(
    onDeleteWG: (WG) -> Unit,
    onManageMembers: (WG) -> Unit,
    onTerminate: () -> Unit
) {
    val allWGs = DataStore.wgs

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── KPI OVERVIEW ─────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    icon = Icons.Default.Home,
                    value = "${allWGs.size}",
                    label = AppStrings.spWgsCount,
                    iconColor = PanelAccent,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    icon = Icons.Default.Group,
                    value = "${DataStore.users.count { it.hasWG }}",
                    label = AppStrings.spWgMembers,
                    iconColor = PanelBlue,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    icon = Icons.Default.PersonOff,
                    value = "${DataStore.getUsersWithoutWG().size}",
                    label = "Homeless",
                    iconColor = PanelAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (allWGs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏚️", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            AppStrings.spWgNoWgs,
                            color = PanelTextDim,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // ── WG CARDS ─────────────────────────────────────────
        items(allWGs, key = { it.id }) { wg ->
            WGAdminCard(
                wg = wg,
                onManageMembers = { onManageMembers(wg) },
                onDelete = { onDeleteWG(wg) }
            )
        }

        // ── TERMINATE SESSION ────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(onClick = onTerminate) {
                    Text("↪ ", color = PanelRed, fontSize = 16.sp)
                    Text(
                        AppStrings.spTerminateSession,
                        color = PanelRed,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// WG ADMIN CARD — Dark card with WG details and actions
// ═══════════════════════════════════════════════════════════════
@Composable
private fun WGAdminCard(
    wg: WG,
    onManageMembers: () -> Unit,
    onDelete: () -> Unit
) {
    val members = DataStore.getWGMembers(wg.id)
    val admin = DataStore.getWGAdmin(wg.id)
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = PanelSurface,
        border = BorderStroke(1.dp, PanelBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ── Top Row: WG Icon + Name + Member count ───────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // WG Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(PanelAccent.copy(alpha = 0.3f), PanelPink.copy(alpha = 0.2f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏠", fontSize = 24.sp)
                }

                Spacer(Modifier.width(12.dp))

                // Name + Address
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        wg.name,
                        color = PanelText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        wg.address,
                        color = PanelTextDim,
                        fontSize = 12.sp
                    )
                }

                // Member count badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PanelAccent.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, PanelAccent.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            tint = PanelAccentLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${members.size}",
                            color = PanelAccentLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Info Row: Code + Rent + Admin ────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Join Code
                Column {
                    Text(
                        AppStrings.spWgCode,
                        color = PanelTextDim,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        wg.joinCode,
                        color = PanelGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                // Rent
                Column {
                    Text(
                        AppStrings.spWgRent,
                        color = PanelTextDim,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "${wg.rentPrice}€",
                        color = PanelAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                // Admin
                Column {
                    Text(
                        AppStrings.spWgAdmin,
                        color = PanelTextDim,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        admin?.name ?: AppStrings.spWgNoAdmin,
                        color = if (admin != null) PanelBlue else PanelRed.copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Members preview (expandable) ─────────────────
            Surface(
                onClick = { expanded = !expanded },
                shape = RoundedCornerShape(10.dp),
                color = PanelSurfaceLight,
                border = BorderStroke(1.dp, PanelBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${AppStrings.spWgMembers} (${members.size})",
                            color = PanelTextDim,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.weight(1f))
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = PanelTextDim,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (expanded) {
                        Spacer(Modifier.height(8.dp))
                        if (members.isEmpty()) {
                            Text(
                                AppStrings.spWgNoMembers,
                                color = PanelTextDim.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        } else {
                            members.forEach { member ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        member.avatarEmoji,
                                        fontSize = 16.sp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        member.name,
                                        color = PanelText,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (member.role == UserRole.ADMIN) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = PanelAccent.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                "👑 ${AppStrings.spWgAdmin}",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                                color = PanelAccentLight,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "${member.points} XP",
                                        color = PanelTextDim,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Action Buttons ───────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Manage Members
                Surface(
                    onClick = onManageMembers,
                    shape = RoundedCornerShape(8.dp),
                    color = PanelSurfaceLight,
                    border = BorderStroke(1.dp, PanelBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            tint = PanelAccentLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            AppStrings.spWgManageMembers,
                            color = PanelAccentLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // Delete WG
                Surface(
                    onClick = onDelete,
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = PanelRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            AppStrings.spWgDelete,
                            color = PanelRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// DELETE WG CONFIRMATION DIALOG
// ═══════════════════════════════════════════════════════════════
@Composable
private fun DeleteWGConfirmDialog(
    wgName: String,
    memberCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "delWg")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "delWgScale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PanelSurface,
            border = BorderStroke(2.dp, PanelRed.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🏚️",
                    fontSize = 44.sp,
                    modifier = Modifier.scale(scale)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    AppStrings.spWgDeleteTitle,
                    color = PanelRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "\"$wgName\"",
                    color = PanelText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    AppStrings.spWgDeleteConfirm,
                    color = PanelTextDim,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )
                if (memberCount > 0) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PanelAmber.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, PanelAmber.copy(alpha = 0.3f))
                    ) {
                        Text(
                            "⚠️ $memberCount ${AppStrings.spWgMembers}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = PanelAmber,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, PanelBorder)
                    ) {
                        Text(AppStrings.cancel, color = PanelTextDim)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PanelRed)
                    ) {
                        Text("🗑️ ${AppStrings.spWgDelete}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// MANAGE MEMBERS DIALOG — Add/Remove members, Set Admin
// ═══════════════════════════════════════════════════════════════
@Composable
private fun ManageMembersDialog(
    wg: WG,
    onDismiss: () -> Unit,
    toast: ToastState
) {
    var refreshKey by remember { mutableIntStateOf(0) }
    val members = remember(refreshKey) { DataStore.getWGMembers(wg.id) }
    val availableUsers = remember(refreshKey) { DataStore.getUsersWithoutWG() }
    val admin = remember(refreshKey) { DataStore.getWGAdmin(wg.id) }
    var showAddSection by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PanelSurface,
            border = BorderStroke(1.dp, PanelBorder),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // ── Header ───────────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏠", fontSize = 22.sp)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            wg.name,
                            color = PanelText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            AppStrings.spWgManageMembers,
                            color = PanelTextDim,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = PanelTextDim)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Current Admin ────────────────────────────
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PanelAccent.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, PanelAccent.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👑", fontSize = 18.sp)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                AppStrings.spWgAdmin,
                                color = PanelTextDim,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                admin?.name ?: AppStrings.spWgNoAdmin,
                                color = if (admin != null) PanelAccentLight else PanelRed.copy(alpha = 0.7f),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ── Members List Header ──────────────────────
                Text(
                    "${AppStrings.spWgMembers} (${members.size})",
                    color = PanelTextDim,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(Modifier.height(8.dp))

                // ── Members List (scrollable) ────────────────
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (members.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                AppStrings.spWgNoMembers,
                                color = PanelTextDim.copy(alpha = 0.5f),
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        members.forEach { member ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = PanelSurfaceLight,
                                border = BorderStroke(1.dp, PanelBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(member.avatarEmoji, fontSize = 20.sp)
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                member.name,
                                                color = PanelText,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            )
                                            if (member.role == UserRole.ADMIN) {
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    "👑",
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                        Text(
                                            "${member.points} XP",
                                            color = PanelTextDim,
                                            fontSize = 11.sp
                                        )
                                    }

                                    // Set as Admin button
                                    if (member.role != UserRole.ADMIN) {
                                        Surface(
                                            onClick = {
                                                DataStore.setWGAdmin(member, wg.id)
                                                refreshKey++
                                                toast.show(AppStrings.spWgAdminSet)
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            color = PanelBlue.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                "👑",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 12.sp
                                            )
                                        }
                                        Spacer(Modifier.width(4.dp))
                                    }

                                    // Remove button
                                    Surface(
                                        onClick = {
                                            DataStore.removeUserFromWG(member)
                                            refreshKey++
                                            toast.show(AppStrings.spWgMemberRemoved)
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        color = PanelRed.copy(alpha = 0.15f)
                                    ) {
                                        Icon(
                                            Icons.Default.PersonRemove,
                                            contentDescription = null,
                                            tint = PanelRed,
                                            modifier = Modifier
                                                .padding(6.dp)
                                                .size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── Add Member Section ───────────────────
                    Spacer(Modifier.height(8.dp))

                    Surface(
                        onClick = { showAddSection = !showAddSection },
                        shape = RoundedCornerShape(10.dp),
                        color = PanelGreen.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, PanelGreen.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (showAddSection) Icons.Default.ExpandLess else Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = PanelGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                AppStrings.spWgAddMember,
                                color = PanelGreen,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    if (showAddSection) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            AppStrings.spWgAvailableUsers,
                            color = PanelTextDim,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(4.dp))

                        if (availableUsers.isEmpty()) {
                            Text(
                                AppStrings.spWgNoAvailable,
                                color = PanelTextDim.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            availableUsers.forEach { availUser ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = PanelSurfaceLight,
                                    border = BorderStroke(1.dp, PanelGreen.copy(alpha = 0.2f)),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(availUser.avatarEmoji, fontSize = 18.sp)
                                        Spacer(Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                availUser.name,
                                                color = PanelText,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                availUser.email,
                                                color = PanelTextDim,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Surface(
                                            onClick = {
                                                DataStore.addUserToWG(availUser, wg.id)
                                                refreshKey++
                                                toast.show(AppStrings.spWgMemberAdded)
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            color = PanelGreen.copy(alpha = 0.15f)
                                        ) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = null,
                                                tint = PanelGreen,
                                                modifier = Modifier
                                                    .padding(6.dp)
                                                    .size(16.dp)
                                            )
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
