package com.example.wgmanager.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wgmanager.data.*
import com.example.wgmanager.ui.components.*
import com.example.wgmanager.ui.navigation.AppScreen
import com.example.wgmanager.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

private val DashPink = Color(0xFFEC4899)
private val DashMagenta = Color(0xFFD946EF)
private val DashPurple = Color(0xFFAB47BC)
private val DashYellow = Color(0xFFFACC15)

@Composable
fun DashboardScreen(onNavigate: (AppScreen) -> Unit, toast: ToastState) {
    val user = DataStore.currentUser ?: return
    val palette = LocalThemePalette.current
    val members = remember { DataStore.getWGMembers() }
    var selectedMember by remember { mutableStateOf<User?>(null) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var stickyText by remember { mutableStateOf(AppStrings.dashWlanPassword) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showQuickStatusDialog by remember { mutableStateOf(false) }
    var showJoinRequests by remember { mutableStateOf(false) }
    
    // Localized strings
    val s = AppStrings

    // Animated ticker
    val recentEvents = remember { listOf(
        "Tom added \"Beer\" to shopping list (2m ago)",
        "Anna completed \"Take out Trash\" ✅",
        "Max earned +10 XP 🏆",
        "Lisa planned Spaghetti for today 🍝"
    )}
    var tickerIdx by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (true) { kotlinx.coroutines.delay(4000); tickerIdx = (tickerIdx + 1) % recentEvents.size } }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Impersonation Banner (Super Admin)
        if (DataStore.isImpersonating()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF59E0B)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("\uD83D\uDC41\uFE0F ${AppStrings.spImpersonating} ${user.name}", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Surface(
                        onClick = {
                            DataStore.stopImpersonation()
                            onNavigate(AppScreen.SYSTEM_PANEL)
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.2f)
                    ) {
                        Text(AppStrings.spStopImpersonation, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Broadcast Banner
        if (DataStore.broadcastMessage.isNotBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFEF4444)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("\uD83D\uDCE2 ${DataStore.broadcastMessage}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = { DataStore.clearBroadcast() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, s.dashDismiss, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            // ══════════════════════════════════════════════════
            // HEADER — Pink/magenta gradient
            // ══════════════════════════════════════════════════
            Box(modifier = Modifier.fillMaxWidth().background(
                Brush.linearGradient(listOf(DashPink, DashMagenta, DashPurple))
            )) {
                Column(modifier = Modifier.statusBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${s.hello}, ${user.name} 👋", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            // Online badge - clickable for status selector
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.Black.copy(alpha = 0.3f),
                                modifier = Modifier.clickable { showStatusDialog = true }
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(
                                        when (user.status) {
                                            UserStatus.ONLINE -> Color(0xFF22C55E)
                                            UserStatus.AWAY -> Color(0xFFF59E0B)
                                            UserStatus.SLEEPING -> Color(0xFF6B7280)
                                            UserStatus.FOCUS -> Color(0xFF3B82F6)
                                            UserStatus.PARTY -> Color(0xFFEC4899)
                                            UserStatus.SHOWER -> Color(0xFF06B6D4)
                                        }
                                    ))
                                    Spacer(Modifier.width(6.dp))
                                    Text(user.status.label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Spacer(Modifier.width(4.dp))
                                    Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                        AvatarCircle(emoji = user.avatarEmoji, size = 52, onClick = { onNavigate(AppScreen.PROFILE) })
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Active Members Row ──
                    Surface(shape = RoundedCornerShape(24.dp), color = Color.Black.copy(alpha = 0.25f)) {
                        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(s.dashActive, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(s.dashNow, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(12.dp))
                            members.take(4).forEach { m ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp).clickable { selectedMember = m }) {
                                    AvatarCircle(emoji = m.avatarEmoji, size = 42, statusDot = when (m.status) {
                                        UserStatus.ONLINE -> Color(0xFF22C55E)
                                        UserStatus.AWAY -> Color(0xFFF59E0B)
                                        UserStatus.SLEEPING -> Color(0xFF9E9E9E)
                                        else -> Color(0xFF3B82F6)
                                    })
                                    Spacer(Modifier.height(2.dp))
                                    Text(m.name, color = Color.White, fontSize = 10.sp, maxLines = 1)
                                }
                            }
                            if (members.size > 4) {
                                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.15f), modifier = Modifier.size(42.dp)) {
                                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.GroupAdd, "More", tint = Color.White, modifier = Modifier.size(20.dp)) }
                                }
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(16.dp))

                // ══════════════════════════════════════════════════
                // DEIN WG ALLTAG header
                // ══════════════════════════════════════════════════
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(DashPink))
                    Spacer(Modifier.width(8.dp))
                    Text(s.dashYourWgLife, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.height(12.dp))

                val pendingCount = remember(refreshKey) { DataStore.shoppingItems.count { it.status == ShoppingStatus.PENDING } }
                val openTasks = remember(refreshKey) { DataStore.tasks.count { !it.completed } }
                val firstItems = remember(refreshKey) { DataStore.shoppingItems.filter { it.status == ShoppingStatus.PENDING }.take(2) }

                // Two cards side-by-side
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
                    // Shopping card (green/teal tint)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        modifier = Modifier.weight(1f).fillMaxHeight().clickable { onNavigate(AppScreen.SHOPPING) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF10B981).copy(alpha = 0.3f)) {
                                    Text("🛒", fontSize = 18.sp, modifier = Modifier.padding(8.dp))
                                }
                                Spacer(Modifier.weight(1f))
                                Text("$pendingCount", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(s.dashShopping, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(6.dp))
                            firstItems.forEach { item ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                    Spacer(Modifier.width(6.dp))
                                    Text(item.name, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                            if (pendingCount > 2) {
                                Text(s.dashAndMore, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }

                    // Tasks card (orange/yellow tint)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                        modifier = Modifier.weight(1f).fillMaxHeight().clickable { onNavigate(AppScreen.CLEANING) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF59E0B).copy(alpha = 0.3f)) {
                                    Text("📋", fontSize = 18.sp, modifier = Modifier.padding(8.dp))
                                }
                                Spacer(Modifier.weight(1f))
                                Text("$openTasks", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(s.dashOpen, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.RemoveCircleOutline, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(s.dashRelaxMode, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ══════════════════════════════════════════════════
                // JOIN REQUESTS — Admin only, when pending
                // ══════════════════════════════════════════════════
                val isAdmin = user.role == UserRole.ADMIN || user.role == UserRole.SUPER_ADMIN
                val joinRequestsList = remember(refreshKey) { DataStore.getPendingRequestsForMyWG() }

                if (isAdmin && joinRequestsList.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF3B82F6)))
                        Spacer(Modifier.width(8.dp))
                        Text("📬 ${s.joinRequests.uppercase()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                        // Notification badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEF4444)
                        ) {
                            Text(
                                "${joinRequestsList.size}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))

                    // Request cards
                    joinRequestsList.forEach { request ->
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFF3B82F6).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Avatar placeholder
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF3B82F6).copy(alpha = 0.25f),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                            Text("👤", fontSize = 20.sp)
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(request.userName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(request.userEmail, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                    }
                                    Text(request.date, color = MaterialTheme.colorScheme.outline, fontSize = 11.sp)
                                }

                                if (request.message.isNotBlank()) {
                                    Spacer(Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.Black.copy(alpha = 0.2f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            "\"${request.message}\"",
                                            modifier = Modifier.padding(10.dp),
                                            color = Color(0xFFCBD5E1),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Light
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                // Accept / Reject buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Accept
                                    Button(
                                        onClick = {
                                            DataStore.acceptJoinRequest(request)
                                            refreshKey++
                                            toast.show(s.requestAcceptedMsg)
                                        },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                    ) {
                                        Text("✅ ${s.acceptBtn}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                    }
                                    // Reject
                                    OutlinedButton(
                                        onClick = {
                                            DataStore.rejectJoinRequest(request)
                                            refreshKey++
                                            toast.show(s.requestRejectedMsg)
                                        },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                                    ) {
                                        Text("❌ ${s.rejectBtn}", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }

                // ══════════════════════════════════════════════════
                // EVENT CARD — gradient pink/magenta with weather
                // ══════════════════════════════════════════════════
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth().clickable { onNavigate(AppScreen.CALENDAR) }
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .background(Brush.linearGradient(listOf(DashPink, DashMagenta)), RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        val sdf = remember { SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY) }
                        val todayDate = remember { Date() }
                        val nextEvent = remember(refreshKey) {
                            DataStore.events
                                .filter { e ->
                                    try {
                                        val ed = sdf.parse(e.date)
                                        ed != null && !ed.before(todayDate)
                                    } catch (_: Exception) { false }
                                }
                                .minByOrNull { e ->
                                    try { sdf.parse(e.date)?.time ?: Long.MAX_VALUE } catch (_: Exception) { Long.MAX_VALUE }
                                }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                // Weather badge
                                Surface(shape = RoundedCornerShape(10.dp), color = Color.White.copy(alpha = 0.2f)) {
                                    Text("☀️ 18°C ${s.dashWeatherToday}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Spacer(Modifier.height(14.dp))
                                if (nextEvent != null) {
                                    Text("${nextEvent.emoji} ${nextEvent.title}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White))
                                        Spacer(Modifier.width(6.dp))
                                        Text("${nextEvent.date} • by ${nextEvent.createdBy}", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                                    }
                                } else {
                                    Text(s.dashNoEvents, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                                    Text(s.dashPlanSomething, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                                }
                            }
                            // Countdown badge on the right
                            if (nextEvent != null) {
                                Spacer(Modifier.width(10.dp))
                                val daysUntil = remember(nextEvent) {
                                    try {
                                        val eventDate = sdf.parse(nextEvent.date)
                                        if (eventDate != null) {
                                            val diff = eventDate.time - todayDate.time
                                            (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
                                        } else 0
                                    } catch (_: Exception) { 0 }
                                }
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.widthIn(min = 88.dp).heightIn(min = 88.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text("📅", fontSize = 18.sp)
                                        if (daysUntil == 0) {
                                            Text(s.today, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                        } else {
                                            Text("$daysUntil", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
                                            Text(
                                                if (daysUntil == 1) s.dayUntil else s.daysUntil,
                                                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ══════════════════════════════════════════════════
                // BLACKBOARD NOTE — Yellow card
                // ══════════════════════════════════════════════════
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = DashYellow,
                    modifier = Modifier.fillMaxWidth().clickable { onNavigate(AppScreen.BLACKBOARD) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Description, null, tint = Color.Black.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(s.dashBlackboardNote, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = Color.Black.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
                            Icon(Icons.Default.Chat, null, tint = Color.Black.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("\"$stickyText\"", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Spacer(Modifier.height(18.dp))

                // ══════════════════════════════════════════════════
                // TOOLS & TEAM — Icon grid
                // ══════════════════════════════════════════════════
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(DashPink))
                    Spacer(Modifier.width(8.dp))
                    Text(s.toolsAndTeam, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ToolButton("🍽️", s.dashMeals, DashPink) { onNavigate(AppScreen.MEAL_PLANNER) }
                    ToolButton("👥", s.crew, Color(0xFF8B5CF6)) { onNavigate(AppScreen.CREW) }
                    ToolButton("🔐", s.vault, Color(0xFF64748B)) { onNavigate(AppScreen.VAULT) }
                    ToolButton("#️⃣", s.dashStatus, Color(0xFFFACC15)) { showQuickStatusDialog = true }
                }

                Spacer(Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ToolButton("💸", s.dashCosts, Color(0xFF10B981)) { onNavigate(AppScreen.RECURRING_COSTS) }
                    ToolButton("🏆", s.dashFame, Color(0xFFFFD700)) { onNavigate(AppScreen.WALL_OF_FAME) }
                    ToolButton("🎫", s.dashGuest, Color(0xFFDB2777)) { onNavigate(AppScreen.GUEST_PASS) }
                    ToolButton("🏠", s.dashSmart, Color(0xFF06B6D4)) { onNavigate(AppScreen.SMART_HOME) }
                }

                Spacer(Modifier.height(14.dp))

                // Statistiken bar
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth().clickable { onNavigate(AppScreen.ANALYTICS) }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PieChart, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(s.stats, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, fontSize = 15.sp, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowForward, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ══════════════════════════════════════════════════
                // FOOTER TICKER
                // ══════════════════════════════════════════════════
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(DashPink))
                    Spacer(Modifier.width(8.dp))
                    AnimatedContent(targetState = tickerIdx, transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) }, label = "ticker") { idx ->
                        Text("${s.dashRecent} ${recentEvents[idx]}", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }

        DashboardBottomNav(current = AppScreen.DASHBOARD, onNavigate = onNavigate, isAdmin = user.role != UserRole.USER)
    }

    // ── Member preview dialog ──
    selectedMember?.let { m ->
        val statusColor = when (m.status) {
            UserStatus.ONLINE -> Color(0xFF22C55E)
            UserStatus.AWAY -> Color(0xFFF59E0B)
            UserStatus.SLEEPING -> Color(0xFF6B7280)
            UserStatus.FOCUS -> Color(0xFF3B82F6)
            UserStatus.PARTY -> Color(0xFFEC4899)
            UserStatus.SHOWER -> Color(0xFF06B6D4)
        }
        AlertDialog(
            onDismissRequest = { selectedMember = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                        IconButton(onClick = { selectedMember = null }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                    AvatarCircle(emoji = m.avatarEmoji, size = 72)
                    Spacer(Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(12.dp), color = statusColor.copy(alpha = 0.2f)) {
                        Text(m.status.label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = statusColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(m.name, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(m.levelTitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (m.bio.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text("\"${m.bio}\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Light)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.background, modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${m.points}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(s.points.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, letterSpacing = 1.sp)
                            }
                        }
                        Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.background, modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(m.role.name, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(s.role.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, letterSpacing = 1.sp)
                            }
                        }
                    }
                    // Contact info
                    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(m.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        }
                    }
                }
            },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { toast.show("${m.name} — ${s.dashMsgSent}"); selectedMember = null },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) { Text(s.dashSendMsg, fontWeight = FontWeight.Bold, color = Color.White) }
                    Button(
                        onClick = { toast.show("${m.name} ${s.dashNudged}"); selectedMember = null },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DashPink)
                    ) { Text(s.dashNudge, fontWeight = FontWeight.Bold, color = Color.White) }
                }
            }
        )
    }

    // ── Status Selector Dialog ──
    if (showStatusDialog) {
        StatusSelectorDialog(
            currentStatus = user.status,
            onSelect = { newStatus ->
                DataStore.currentUser = user.copy(status = newStatus)
                showStatusDialog = false
                toast.show("${s.dashStatusChanged} ${newStatus.label}")
            },
            onDismiss = { showStatusDialog = false }
        )
    }

    // ── Quick Status Presets Dialog ──
    if (showQuickStatusDialog) {
        QuickStatusDialog(
            onSelect = { preset ->
                showQuickStatusDialog = false
                toast.show("Status: $preset")
            },
            onDismiss = { showQuickStatusDialog = false }
        )
    }
}

@Composable
private fun ToolButton(emoji: String, label: String, tint: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.size(60.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(emoji, fontSize = 26.sp)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatusSelectorDialog(
    currentStatus: UserStatus,
    onSelect: (UserStatus) -> Unit,
    onDismiss: () -> Unit
) {
    val statuses = listOf(
        UserStatus.ONLINE to ("🟢" to AppStrings.dashStatusOnline),
        UserStatus.AWAY to ("🟡" to AppStrings.dashStatusAway),
        UserStatus.SLEEPING to ("😴" to AppStrings.dashStatusSleeping),
        UserStatus.FOCUS to ("🎯" to AppStrings.dashStatusFocus),
        UserStatus.PARTY to ("🎉" to AppStrings.dashStatusParty),
        UserStatus.SHOWER to ("🚿" to AppStrings.dashStatusShower)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(AppStrings.dashChangeStatus, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(AppStrings.dashHowAreYou, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                statuses.forEach { (status, details) ->
                    val (emoji, description) = details
                    val isSelected = status == currentStatus
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) DashPink.copy(alpha = 0.2f) else MaterialTheme.colorScheme.background,
                        border = if (isSelected) BorderStroke(2.dp, DashPink) else null,
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(status) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(emoji, fontSize = 24.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(status.label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, null, tint = DashPink)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun QuickStatusDialog(
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val presets = listOf(
        "📶" to AppStrings.noWifi,
        "💧" to AppStrings.noWater,
        "🎉" to AppStrings.partyTonight,
        "🤫" to AppStrings.dashPresetQuiet,
        "👥" to AppStrings.dashPresetGuests,
        "🍕" to AppStrings.dashPresetFoodReady,
        "🧹" to AppStrings.dashPresetCleaning,
        "📦" to AppStrings.dashPresetPackage
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(AppStrings.quickStatus, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(AppStrings.dashShareWithWg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { (emoji, text) ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier.weight(1f).clickable { onSelect("$emoji $text") }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(emoji, fontSize = 20.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                        // Pad last row if odd number
                        if (row.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}
