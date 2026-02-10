package com.example.wgmanager.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wgmanager.data.AppStrings
import com.example.wgmanager.ui.navigation.AppScreen
import com.example.wgmanager.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════════════════════
// TOAST SYSTEM (React-style notifications)
// ═══════════════════════════════════════════════════════════════
enum class ToastType { SUCCESS, INFO, ERROR }

data class ToastData(val message: String, val type: ToastType = ToastType.SUCCESS, val id: Long = System.currentTimeMillis())

class ToastState {
    var current by mutableStateOf<ToastData?>(null)
        private set

    fun show(message: String, type: ToastType = ToastType.SUCCESS) {
        current = ToastData(message, type)
    }

    fun dismiss() { current = null }
}

@Composable
fun rememberToastState() = remember { ToastState() }

@Composable
fun WGToastHost(state: ToastState, modifier: Modifier = Modifier) {
    val toast = state.current
    LaunchedEffect(toast) {
        if (toast != null) { delay(3000); state.dismiss() }
    }
    AnimatedVisibility(
        visible = toast != null,
        enter = slideInVertically(tween(250)) { -it } + fadeIn(),
        exit = slideOutVertically(tween(200)) { -it } + fadeOut(),
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        toast?.let { t ->
            val (bg, emoji) = when (t.type) {
                ToastType.SUCCESS -> WGSuccess to "✅"
                ToastType.INFO -> WGInfo to "ℹ️"
                ToastType.ERROR -> WGDanger to "❌"
            }
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = bg),
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(14.dp))
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(emoji, fontSize = 18.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(t.message, color = Color.White, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { state.dismiss() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// PHONE SHELL (status bar + home indicator like React prototype)
// ═══════════════════════════════════════════════════════════════
@Composable
fun PhoneStatusBar() {
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        Text("12:00", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("5G", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("100%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("🔋", fontSize = 10.sp)
        }
    }
}

@Composable
fun HomeIndicator() {
    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.width(134.dp).height(5.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
    }
}

// ═══════════════════════════════════════════════════════════════
// ANIMATED GRADIENT HEADER
// ═══════════════════════════════════════════════════════════════
@Composable
fun GradientHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null
) {
    val palette = LocalThemePalette.current
    Box(
        modifier = Modifier.fillMaxWidth().background(
            Brush.linearGradient(listOf(palette.gradientStart, palette.gradientEnd))
        )
    ) {
        // Floating shapes decoration
        Box(modifier = Modifier.size(80.dp).offset(x = (-20).dp, y = (-10).dp)
            .clip(CircleShape).background(Color.White.copy(alpha = 0.06f)))
        Box(modifier = Modifier.size(50.dp).offset(x = 280.dp, y = 40.dp)
            .clip(CircleShape).background(Color.White.copy(alpha = 0.04f)))

        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    subtitle?.let { Text(it, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall) }
                }
                trailing?.invoke()
            }
            content?.invoke(this)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// ANIMATED LIST ITEM (staggered entry)
// ═══════════════════════════════════════════════════════════════
@Composable
fun AnimatedListItem(index: Int, content: @Composable () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val offset = remember { Animatable(24f) }
    LaunchedEffect(Unit) {
        delay(index * 40L)
        launch { alpha.animateTo(1f, tween(250)) }
        offset.animateTo(0f, tween(250, easing = FastOutSlowInEasing))
    }
    Box(modifier = Modifier.offset(y = offset.value.dp).graphicsLayer(alpha = alpha.value)) { content() }
}

// ═══════════════════════════════════════════════════════════════
// WG CARD (pressable with scale)
// ═══════════════════════════════════════════════════════════════
@Composable
fun WGCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, spring(dampingRatio = 0.6f), label = "cardScale")
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier.scale(scale).then(
            if (onClick != null) Modifier.clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                onClick = onClick
            ) else Modifier
        ),
        content = content
    )
}

// ═══════════════════════════════════════════════════════════════
// STAT CARD
// ═══════════════════════════════════════════════════════════════
@Composable
fun StatCard(emoji: String, label: String, value: String, modifier: Modifier = Modifier) {
    WGCard(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// AVATAR CIRCLE
// ═══════════════════════════════════════════════════════════════
@Composable
fun AvatarCircle(
    emoji: String = "👤",
    name: String = "",
    size: Int = 44,
    borderColor: Color = Color.Transparent,
    statusDot: Color? = null,
    onClick: (() -> Unit)? = null
) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size.dp).then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)) {
        Surface(
            modifier = Modifier.size(size.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(emoji.ifEmpty { name.firstOrNull()?.toString() ?: "?" }, fontSize = (size / 2).sp)
            }
        }
        if (statusDot != null) {
            Box(modifier = Modifier.align(Alignment.BottomEnd).size(12.dp).clip(CircleShape).background(statusDot))
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// SECTION HEADER
// ═══════════════════════════════════════════════════════════════
@Composable
fun SectionHeader(title: String, action: @Composable (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        action?.invoke()
    }
}

// ═══════════════════════════════════════════════════════════════
// STATUS CHIP
// ═══════════════════════════════════════════════════════════════
@Composable
fun StatusChip(text: String, color: Color) {
    Surface(shape = RoundedCornerShape(20.dp), color = color.copy(alpha = 0.12f)) {
        Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

// ═══════════════════════════════════════════════════════════════
// XP CELEBRATION OVERLAY
// ═══════════════════════════════════════════════════════════════
@Composable
fun XpCelebration(visible: Boolean, points: Int = 10) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(tween(200)) + fadeIn(),
        exit = scaleOut(tween(300)) + fadeOut()
    ) {
        Text("+${points} XP ✨", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = WGSuccess, textAlign = TextAlign.Center)
    }
}

// ═══════════════════════════════════════════════════════════════
// EMPTY STATE
// ═══════════════════════════════════════════════════════════════
@Composable
fun EmptyState(emoji: String, message: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

// ═══════════════════════════════════════════════════════════════
// DASHBOARD BOTTOM NAV
// ═══════════════════════════════════════════════════════════════
@Composable
fun DashboardBottomNav(
    current: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    isAdmin: Boolean = false
) {
    val palette = LocalThemePalette.current
    val s = AppStrings
    val items = listOf(
        Triple("🏠", s.dashboard, AppScreen.DASHBOARD),
        Triple("🛒", s.shoppingList, AppScreen.SHOPPING),
        Triple("📅", s.calendar, AppScreen.CALENDAR),
        Triple("👥", s.crew, AppScreen.CREW),
        Triple("👤", s.profile, AppScreen.PROFILE)
    )
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (emoji, label, screen) ->
                val selected = current == screen
                val color = if (selected) palette.primary else MaterialTheme.colorScheme.onSurfaceVariant
                Column(
                    modifier = Modifier
                        .clickable(onClick = { onNavigate(screen) })
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(emoji, fontSize = if (selected) 22.sp else 20.sp)
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = color
                    )
                    if (selected) {
                        Spacer(Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(palette.primary)
                        )
                    }
                }
            }
        }
    }
}
