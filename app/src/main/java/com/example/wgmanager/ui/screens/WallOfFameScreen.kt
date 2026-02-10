package com.example.wgmanager.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wgmanager.data.*
import com.example.wgmanager.ui.components.*
import com.example.wgmanager.ui.navigation.AppScreen
import com.example.wgmanager.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallOfFameScreen(
    onNavigate: (AppScreen) -> Unit,
    toast: ToastState
) {
    val palette = LocalThemePalette.current
    val str = AppStrings
    val leaderboard = DataStore.getLeaderboard()
    val currentUser = DataStore.currentUser
    var refreshKey by remember { mutableIntStateOf(0) }

    key(refreshKey) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text("🏆 ${str.wallOfFame}", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { onNavigate(AppScreen.DASHBOARD) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
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
                                        listOf(
                                            Color(0xFFFFD700),
                                            Color(0xFFFF8C00)
                                        )
                                    ),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(str.wallOfFameSubtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                                Spacer(Modifier.height(12.dp))
                                if (leaderboard.isNotEmpty()) {
                                    val champion = leaderboard.first()
                                    // Animated crown
                                    val infiniteTransition = rememberInfiniteTransition(label = "crown")
                                    val crownScale by infiniteTransition.animateFloat(
                                        initialValue = 1f,
                                        targetValue = 1.15f,
                                        animationSpec = infiniteRepeatable(
                                            tween(800, easing = EaseInOutCubic),
                                            RepeatMode.Reverse
                                        ),
                                        label = "crownPulse"
                                    )
                                    Text(
                                        "👑",
                                        fontSize = 48.sp,
                                        modifier = Modifier.scale(crownScale)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        champion.first.avatarEmoji,
                                        fontSize = 40.sp
                                    )
                                    Text(
                                        champion.first.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    )
                                    Text(
                                        "${str.crownHolder} • ${champion.second} ${str.pointsLabel}",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Section header
                item {
                    Text(
                        str.leaderboard,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Leaderboard entries
                itemsIndexed(leaderboard) { index, (user, score) ->
                    AnimatedListItem(index = index) {
                        LeaderboardEntry(
                            rank = index,
                            user = user,
                            score = score,
                            totalMembers = leaderboard.size,
                            isCurrentUser = user.id == currentUser?.id,
                            canInteract = user.id != currentUser?.id,
                            onKudos = {
                                DataStore.sendKudos(user.name)
                                toast.show(str.kudosSent)
                                refreshKey++
                            },
                            onShame = {
                                DataStore.sendShame(user.name)
                                toast.show(str.shameSent)
                                refreshKey++
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardEntry(
    rank: Int,
    user: User,
    score: Int,
    totalMembers: Int,
    isCurrentUser: Boolean,
    canInteract: Boolean,
    onKudos: () -> Unit,
    onShame: () -> Unit
) {
    val str = AppStrings
    val palette = LocalThemePalette.current
    val badge = DataStore.getBadgeForRank(rank, totalMembers)
    val bgColor = when (rank) {
        0 -> Color(0x33FFD700) // gold tint
        totalMembers - 1 -> Color(0x33FF4444) // red tint
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank number
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (rank) {
                            0 -> Color(0xFFFFD700)
                            1 -> Color(0xFFC0C0C0)
                            2 -> Color(0xFFCD7F32)
                            else -> Color.White.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "#${rank + 1}",
                    color = if (rank < 3) Color.Black else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            // Avatar + name
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(user.avatarEmoji, fontSize = 24.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        user.name + if (isCurrentUser) str.youSuffix else "",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(badge, fontSize = 18.sp)
                }
                Text(
                    user.levelTitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            // Score + actions
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$score",
                    color = when (rank) {
                        0 -> Color(0xFFFFD700)
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    AppStrings.pointsLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )

                if (canInteract) {
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onKudos, modifier = Modifier.size(28.dp)) {
                            Text("🌟", fontSize = 16.sp)
                        }
                        IconButton(onClick = onShame, modifier = Modifier.size(28.dp)) {
                            Text("😬", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
