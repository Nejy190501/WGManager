package com.example.wgmanager.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.wgmanager.data.*
import com.example.wgmanager.ui.components.*
import com.example.wgmanager.ui.navigation.AppScreen
import com.example.wgmanager.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Purple gradient colors for Crew
private val CrewPurpleDark = Color(0xFF4C1D95)
private val CrewPurpleLight = Color(0xFF8B5CF6)
private val CrewPurpleMid = Color(0xFF7C3AED)

@Composable
fun CrewScreen(onNavigate: (AppScreen) -> Unit, toast: ToastState) {
    val s = AppStrings
    val user = DataStore.currentUser ?: return
    val isAdmin = user.role != UserRole.USER
    var refreshKey by remember { mutableIntStateOf(0) }
    val members = remember(refreshKey) { DataStore.getWGMembers() }
    val scope = rememberCoroutineScope()

    // Randomizer state
    var spinning by remember { mutableStateOf(false) }
    var spinResult by remember { mutableStateOf<String?>(null) }

    // Dialogs
    var selectedMember by remember { mutableStateOf<User?>(null) }
    var showAddMember by remember { mutableStateOf(false) }
    var showKickConfirm by remember { mutableStateOf<User?>(null) }
    var showPendingRequests by remember { mutableStateOf(false) }

    // Pending join requests for admin
    val pendingRequests = remember(refreshKey) { DataStore.getPendingRequestsForMyWG() }
    val hasPending = isAdmin && pendingRequests.isNotEmpty()

    // AI Schiedsrichter state
    var aiJudging by remember { mutableStateOf(false) }
    var aiVerdict by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onNavigate(AppScreen.DASHBOARD) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, s.back, tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.width(4.dp))
            Text(s.wgCrew, color = MaterialTheme.colorScheme.onSurface, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (isAdmin) {
                if (hasPending) {
                    // Show pending requests badge button
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF97316),
                        modifier = Modifier
                            .clickable { showPendingRequests = true }
                            .padding(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, s.pendingRequests, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text(
                                "${s.pendingRequests} (${pendingRequests.size})",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    IconButton(onClick = { showAddMember = true }) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(CrewPurpleMid),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PersonAdd, s.addMember, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            // ── TOP 3 PODIUM ──────────────────────────────────────
            item {
                val sorted = members.sortedByDescending { it.points }
                if (sorted.size >= 3) {
                    val gold = sorted[0]
                    val silver = sorted[1]
                    val bronze = sorted[2]

                    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                            Text(
                                "🏆 ${s.top3Label}",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                // Silver (2nd) - shorter podium
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🥈", fontSize = 20.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier.size(52.dp).clip(CircleShape)
                                            .background(Color(0xFFC0C0C0).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) { Text(silver.avatarEmoji, fontSize = 26.sp) }
                                    Spacer(Modifier.height(4.dp))
                                    Text(silver.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                                    Text("${silver.points} ${s.xpLabel}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier.width(60.dp).height(50.dp)
                                            .background(Color(0xFFC0C0C0).copy(alpha = 0.3f), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) { Text("2", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFC0C0C0)) }
                                }

                                // Gold (1st) - tallest podium
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("👑", fontSize = 26.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier.size(64.dp).clip(CircleShape)
                                            .background(Color(0xFFFFD700).copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center
                                    ) { Text(gold.avatarEmoji, fontSize = 32.sp) }
                                    Spacer(Modifier.height(4.dp))
                                    Text(gold.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                                    Text("${gold.points} ${s.xpLabel}", fontSize = 10.sp, color = Color(0xFFFFD700))
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier.width(60.dp).height(70.dp)
                                            .background(Color(0xFFFFD700).copy(alpha = 0.3f), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) { Text("1", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFFFFD700)) }
                                }

                                // Bronze (3rd) - shortest podium
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🥉", fontSize = 20.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier.size(48.dp).clip(CircleShape)
                                            .background(Color(0xFFCD7F32).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) { Text(bronze.avatarEmoji, fontSize = 24.sp) }
                                    Spacer(Modifier.height(4.dp))
                                    Text(bronze.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                                    Text("${bronze.points} ${s.xpLabel}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier.width(60.dp).height(35.dp)
                                            .background(Color(0xFFCD7F32).copy(alpha = 0.3f), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) { Text("3", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFCD7F32)) }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }

            // ── AI SCHIEDSRICHTER CARD ────────────────────────────
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(CrewPurpleDark, CrewPurpleLight)))
                            .padding(24.dp)
                    ) {
                        // Decorative shapes
                        Box(
                            modifier = Modifier.size(60.dp).align(Alignment.TopEnd).offset(x = 10.dp, y = (-10).dp)
                                .clip(RoundedCornerShape(14.dp)).rotate(15f)
                                .background(Color.White.copy(alpha = 0.08f))
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("⚖\uFE0F ${s.aiReferee}",
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Bold, letterSpacing = 2.sp,
                                style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(s.aiRefereeDesc,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp, textAlign = TextAlign.Center)

                            Spacer(Modifier.height(16.dp))

                            // Result area
                            if (aiVerdict != null) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.White.copy(alpha = 0.1f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        aiVerdict!!,
                                        modifier = Modifier.padding(16.dp),
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                            }

                            // Conflict resolver spinner
                            Box(
                                modifier = Modifier.size(70.dp).clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    spinResult ?: "⚖\uFE0F",
                                    fontSize = if (spinResult != null) 26.sp else 32.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (spinResult != null) {
                                Spacer(Modifier.height(4.dp))
                                Text(spinResult!!, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }

                            Spacer(Modifier.height(16.dp))

                            // Two buttons: Who does it + AI Judge
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = {
                                        if (!spinning && members.isNotEmpty()) {
                                            spinning = true; spinResult = null
                                            scope.launch {
                                                repeat(8) {
                                                    spinResult = members.random().let { "${it.avatarEmoji}" }
                                                    delay(150)
                                                }
                                                val winner = members.random()
                                                spinResult = "${winner.avatarEmoji} ${winner.name}"
                                                spinning = false
                                                toast.show("${winner.name} ${s.wasChosen}")
                                            }
                                        }
                                    },
                                    enabled = !spinning && !aiJudging,
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CrewPurpleLight,
                                        disabledContainerColor = CrewPurpleLight.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Text(
                                        if (spinning) "..." else s.whoDoesIt,
                                        fontWeight = FontWeight.Bold, fontSize = 13.sp
                                    )
                                }
                                Button(
                                    onClick = {
                                        if (!aiJudging) {
                                            aiJudging = true; aiVerdict = null
                                            scope.launch {
                                                delay(2000)
                                                val verdicts = listOf(
                                                    "🧹 ${members.random().name} hat diese Woche am wenigsten geputzt. Faire Lösung: Küche übernehmen!",
                                                    "🍕 ${members.random().name} hat zuletzt bestellt. Nächstes Mal zahlt ${members.random().name}!",
                                                    "📊 Alle haben gleich viel beigetragen. Weiter so, Team! 🎉",
                                                    "⚖\uFE0F ${members.random().name} hat die meisten Punkte — dafür heute frei! Der Rest: Gemeinschaftsaufgabe!",
                                                    "🗳️ Demokratische Abstimmung empfohlen. Jeder gibt eine Stimme ab!"
                                                )
                                                aiVerdict = verdicts.random()
                                                aiJudging = false
                                            }
                                        }
                                    },
                                    enabled = !aiJudging && !spinning,
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF10B981),
                                        disabledContainerColor = Color(0xFF10B981).copy(alpha = 0.5f)
                                    )
                                ) {
                                    Text(
                                        if (aiJudging) s.aiJudging else s.judgeUs,
                                        fontWeight = FontWeight.Bold, fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Section header
            item {
                Text(
                    "${s.membersLabel} (${members.size})",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                )
            }

            // Member list with XP display
            itemsIndexed(members.sortedByDescending { it.points }, key = { _, m -> m.id }) { idx, member ->
                val maxPoints = members.maxOfOrNull { it.points } ?: 1
                AnimatedListItem(index = idx) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth().clickable { selectedMember = member }
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Rank badge
                                val rank = idx + 1
                                val rankEmoji = when (rank) { 1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> "#$rank" }
                                Box(modifier = Modifier.width(28.dp), contentAlignment = Alignment.Center) {
                                    Text(rankEmoji, fontSize = if (rank <= 3) 18.sp else 12.sp, fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(Modifier.width(8.dp))

                                // Avatar with status dot
                                Box {
                                    AvatarCircle(
                                        emoji = member.avatarEmoji,
                                        size = 48,
                                        statusDot = when (member.status) {
                                            UserStatus.ONLINE -> WGSuccess
                                            UserStatus.AWAY -> WGWarning
                                            else -> MaterialTheme.colorScheme.outline
                                        }
                                    )
                                }
                                Spacer(Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(member.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        if (member.role == UserRole.ADMIN || member.role == UserRole.SUPER_ADMIN) {
                                            Spacer(Modifier.width(6.dp))
                                            Text("👑", fontSize = 14.sp)
                                        }
                                    }
                                    Text(
                                        member.levelTitle,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                // XP badge
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = CrewPurpleMid.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        "${member.points} ${s.xpLabel}",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        color = CrewPurpleMid,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            // XP progress bar
                            LinearProgressIndicator(
                                progress = { (member.points.toFloat() / maxPoints.toFloat()).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(3.dp),
                                color = CrewPurpleMid,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }

        DashboardBottomNav(current = AppScreen.CREW, onNavigate = onNavigate, isAdmin = isAdmin)
    }

    // Member detail dialog
    selectedMember?.let { m ->
        Dialog(onDismissRequest = { selectedMember = null }) {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    // Close button
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(onClick = { selectedMember = null }, modifier = Modifier.align(Alignment.TopEnd).size(28.dp)) {
                            Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    AvatarCircle(emoji = m.avatarEmoji, size = 72)
                    Spacer(Modifier.height(6.dp))

                    // Status badge
                    Surface(shape = RoundedCornerShape(12.dp), color = CrewPurpleMid.copy(alpha = 0.15f)) {
                        Text(m.status.label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = CrewPurpleMid, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(m.name, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(m.levelTitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(Modifier.height(16.dp))

                    // Points & Role boxes
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${m.points}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(s.points.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                            }
                        }
                        Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(m.role.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(s.role.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                            }
                        }
                    }

                    if (m.bio.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text("\"${m.bio}\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { toast.show("${m.name} ${s.dashNudged}"); selectedMember = null },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CrewPurpleMid)
                    ) { Text(s.dashNudge, fontWeight = FontWeight.Bold) }

                    if (isAdmin && m.id != user.id) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showKickConfirm = m; selectedMember = null },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = WGDanger)
                        ) { Text(s.kickLabel, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }

    // Kick confirmation
    showKickConfirm?.let { m ->
        AlertDialog(
            onDismissRequest = { showKickConfirm = null },
            title = { Text("${m.name} ${s.kickConfirm}", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text(s.willBeRemoved, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = {
                Button(onClick = {
                    m.hasWG = false; m.wgId = ""; DataStore.syncUser(m)
                    toast.show("${m.name} ${s.removedMsg}", ToastType.ERROR)
                    showKickConfirm = null; refreshKey++
                }, colors = ButtonDefaults.buttonColors(containerColor = WGDanger)) { Text(s.kickLabel) }
            },
            dismissButton = { TextButton(onClick = { showKickConfirm = null }) { Text(s.cancel, color = MaterialTheme.colorScheme.onSurface) } }
        )
    }

    // Add member dialog
    if (showAddMember) {
        var newName by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddMember = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(s.newMember, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = newName, onValueChange = { newName = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp), singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedBorderColor = CrewPurpleMid,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        placeholder = { Text(s.enterName, color = MaterialTheme.colorScheme.outline) }
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showAddMember = false }, modifier = Modifier.weight(1f)) {
                            Text(s.cancel, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Button(
                            onClick = {
                                if (newName.isNotBlank()) {
                                    val wgId = user.wgId
                                    val email = "${newName.lowercase().replace(" ", "")}@wg.com"
                                    val newUser = User(name = newName, email = email, wgId = wgId, hasWG = true, avatarEmoji = "👤")
                                    DataStore.addUser(newUser)
                                    toast.show("$newName ${s.addedToWg}")
                                    showAddMember = false; refreshKey++
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CrewPurpleMid)
                        ) { Text(s.add, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }

    // Pending requests dialog
    if (showPendingRequests) {
        val requests = DataStore.getPendingRequestsForMyWG()
        Dialog(onDismissRequest = { showPendingRequests = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "📬 ${s.pendingRequests} (${requests.size})",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        IconButton(
                            onClick = { showPendingRequests = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    if (requests.isEmpty()) {
                        Text(
                            s.noPendingRequests,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            requests.forEach { request ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFF97316).copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("👤", fontSize = 20.sp)
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    request.userName,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                )
                                                Text(
                                                    request.userEmail,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 12.sp
                                                )
                                            }
                                            Text(
                                                request.date,
                                                color = MaterialTheme.colorScheme.outline,
                                                fontSize = 11.sp
                                            )
                                        }

                                        if (request.message.isNotBlank()) {
                                            Spacer(Modifier.height(10.dp))
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.background
                                            ) {
                                                Text(
                                                    "\"${request.message}\"",
                                                    modifier = Modifier.padding(12.dp),
                                                    color = Color(0xFFCBD5E1),
                                                    fontSize = 13.sp,
                                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                                )
                                            }
                                        }

                                        Spacer(Modifier.height(12.dp))

                                        // Accept / Reject buttons
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    DataStore.rejectJoinRequest(request)
                                                    toast.show("${s.requestRejected} ❌", ToastType.ERROR)
                                                    refreshKey++
                                                    if (DataStore.getPendingRequestsForMyWG().isEmpty()) {
                                                        showPendingRequests = false
                                                    }
                                                },
                                                modifier = Modifier.weight(1f).height(40.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = WGDanger
                                                ),
                                                border = BorderStroke(1.dp, WGDanger.copy(alpha = 0.4f))
                                            ) {
                                                Text(
                                                    s.rejectRequest,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            }
                                            Button(
                                                onClick = {
                                                    DataStore.acceptJoinRequest(request)
                                                    toast.show("${s.requestAccepted} ✅ 🎉")
                                                    refreshKey++
                                                    if (DataStore.getPendingRequestsForMyWG().isEmpty()) {
                                                        showPendingRequests = false
                                                    }
                                                },
                                                modifier = Modifier.weight(1f).height(40.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF22C55E)
                                                )
                                            ) {
                                                Text(
                                                    s.acceptRequest,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color.White
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
}
