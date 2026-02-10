package com.example.wgmanager.ui.screens

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

private val BoardBlue = Color(0xFF3B82F6)
private val BoardBlueLight = Color(0xFF60A5FA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlackboardScreen(onNavigate: (AppScreen) -> Unit, toast: ToastState) {
    val s = AppStrings
    val user = DataStore.currentUser ?: return
    val isAdmin = user.role != UserRole.USER
    var refreshKey by remember { mutableIntStateOf(0) }
    var showAddTicket by remember { mutableStateOf(false) }

    val tickets = remember(refreshKey) { DataStore.tickets.sortedByDescending { it.date } }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Blue gradient header
        Box(
            modifier = Modifier.fillMaxWidth().background(
                Brush.linearGradient(listOf(BoardBlue, BoardBlueLight))
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onNavigate(AppScreen.DASHBOARD) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, s.back, tint = Color.White)
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("📋", fontSize = 22.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(s.pinnwand, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.2f)) {
                        Text("${tickets.size} ${s.entriesCount}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp), contentPadding = PaddingValues(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (tickets.isEmpty()) {
                item { EmptyState(emoji = "📋", message = s.noEntries) }
            }

            itemsIndexed(tickets, key = { _, t -> t.id }) { idx, ticket ->
                AnimatedListItem(index = idx) {
                    TicketCard(ticket = ticket, user = user, isAdmin = isAdmin, toast = toast, onRefresh = { refreshKey++ })
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }

        DashboardBottomNav(current = AppScreen.BLACKBOARD, onNavigate = onNavigate, isAdmin = isAdmin)
    }

    // FAB
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        FloatingActionButton(
            onClick = { showAddTicket = true },
            containerColor = BoardBlue,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.padding(end = 24.dp, bottom = 90.dp).size(56.dp)
        ) { Icon(Icons.Default.Edit, "Add", modifier = Modifier.size(24.dp)) }
    }

    // Add ticket dialog
    if (showAddTicket) {
        var text by remember { mutableStateOf("") }
        var type by remember { mutableStateOf(TicketType.COMPLAINT) }
        var pollOptions by remember { mutableStateOf("") }
        var typeExpanded by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showAddTicket = false }) {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(s.newEntryLabel, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = { showAddTicket = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // Type selector chips
                    Text(s.typeLabel, color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TicketType.entries.forEach { t ->
                            val emoji = when (t) { TicketType.COMPLAINT -> "😤"; TicketType.KUDOS -> "🌟"; TicketType.POLL -> "📊" }
                            val label = when (t) { TicketType.COMPLAINT -> s.complaint; TicketType.KUDOS -> s.kudos; TicketType.POLL -> s.poll }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (type == t) BoardBlue.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                border = if (type == t) ButtonDefaults.outlinedButtonBorder(enabled = true) else null,
                                modifier = Modifier.clickable { type = t }
                            ) {
                                Text("$emoji $label", modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    color = if (type == t) BoardBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = text, onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), minLines = 2,
                        placeholder = { Text(s.writeMessage, color = MaterialTheme.colorScheme.outline) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedBorderColor = BoardBlue,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    if (type == TicketType.POLL) {
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = pollOptions, onValueChange = { pollOptions = it },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                            placeholder = { Text(s.commaSeparated, color = MaterialTheme.colorScheme.outline) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedBorderColor = BoardBlue,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface, focusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    Spacer(Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { showAddTicket = false }, modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                        ) { Text(s.cancel) }
                        Button(onClick = {
                            if (text.isNotBlank()) {
                                val options = if (type == TicketType.POLL) pollOptions.split(",").map { it.trim() }.filter { it.isNotBlank() } else emptyList()
                                DataStore.addTicket(Ticket(type = type, text = text, author = user.name, pollOptions = options))
                                toast.show(s.entryCreated); showAddTicket = false; refreshKey++
                            }
                        }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BoardBlue)
                        ) { Text(s.postBtn, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketCard(ticket: Ticket, user: User, isAdmin: Boolean, toast: ToastState, onRefresh: () -> Unit) {
    val s = AppStrings
    val (typeEmoji, typeColor, typeLabel) = when (ticket.type) {
        TicketType.COMPLAINT -> Triple("😤", WGDanger, s.complaintUpper)
        TicketType.KUDOS -> Triple("🌟", WGSuccess, s.kudosUpper)
        TicketType.POLL -> Triple("📊", WGInfo, s.pollUpper)
    }

    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(typeEmoji, fontSize = 22.sp)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(shape = RoundedCornerShape(6.dp), color = typeColor) {
                            Text(typeLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                        if (ticket.isSolved) {
                            Surface(shape = RoundedCornerShape(6.dp), color = WGSuccess) {
                                Text(s.resolvedLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    }
                    val author = DataStore.users.find { it.name == ticket.author }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarCircle(emoji = author?.avatarEmoji ?: "👤", size = 16)
                        Spacer(Modifier.width(4.dp))
                        Text("${ticket.author} • ${ticket.date}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(ticket.text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)

            // Poll
            if (ticket.type == TicketType.POLL && ticket.pollOptions.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                val totalVotes = ticket.pollVotes.size
                val userVote = ticket.pollVotes[user.name]

                ticket.pollOptions.forEach { option ->
                    val votes = ticket.pollVotes.count { it.value == option }
                    val pct = if (totalVotes > 0) votes * 100 / totalVotes else 0

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (userVote == option) BoardBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable {
                            if (userVote == null) {
                                ticket.pollVotes[user.name] = option
                                DataStore.syncTicket(ticket)
                                toast.show("${s.votedFor} $option! 🗳️")
                                onRefresh()
                            }
                        }
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (userVote == option) Icon(Icons.Default.CheckCircle, null, tint = BoardBlue, modifier = Modifier.size(18.dp))
                            Text(option, modifier = Modifier.weight(1f).padding(start = if (userVote == option) 6.dp else 0.dp),
                                fontWeight = if (userVote == option) FontWeight.SemiBold else FontWeight.Normal, color = MaterialTheme.colorScheme.onSurface)
                            Text("$votes ($pct%)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Admin solve button
            if (isAdmin && !ticket.isSolved) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    ticket.isSolved = true; DataStore.syncTicket(ticket); toast.show(s.markedResolved); onRefresh()
                }, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WGSuccess)) {
                    Text(s.markResolved)
                }
            }
        }
    }
}
