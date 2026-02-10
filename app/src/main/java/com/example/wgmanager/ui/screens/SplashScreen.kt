package com.example.wgmanager.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wgmanager.data.AppLanguage
import com.example.wgmanager.data.AppStrings
import kotlinx.coroutines.delay
import kotlin.math.sin

private val SplashPurple = Color(0xFF7C3AED)
private val SplashPurpleLight = Color(0xFF8B5CF6)

// Data class for floating emoji configuration
private data class FloatingEmoji(
    val emoji: String,
    val x: Dp,
    val y: Dp,
    val alpha: Float,
    val floatOffset: Float = 0f, // Phase offset for animation
    val scale: Float = 1f
)

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val logoScale = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }
    
    // Infinite floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "floatAnim"
    )

    LaunchedEffect(Unit) {
        logoScale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 300f))
        titleAlpha.animateTo(1f, tween(400))
        delay(200)
        subtitleAlpha.animateTo(1f, tween(400))
        delay(1800)
        onFinished()
    }

    val emojis = remember {
        listOf(
            // ═══ Top Left Corner ═══
            FloatingEmoji("🍴", 20.dp, 50.dp, 0.15f, floatOffset = 0f),
            FloatingEmoji("🏠", 80.dp, 90.dp, 0.12f, floatOffset = 0.5f),
            FloatingEmoji("📡", 35.dp, 140.dp, 0.10f, floatOffset = 1.0f),
            FloatingEmoji("🎮", 100.dp, 180.dp, 0.08f, floatOffset = 1.5f),
            
            // ═══ Top Right Corner ═══
            FloatingEmoji("🛋️", 270.dp, 60.dp, 0.12f, floatOffset = 2f),
            FloatingEmoji("📱", 320.dp, 100.dp, 0.15f, floatOffset = 2.5f),
            FloatingEmoji("🏢", 250.dp, 150.dp, 0.10f, floatOffset = 3f),
            FloatingEmoji("🔑", 300.dp, 200.dp, 0.08f, floatOffset = 3.5f),
            
            // ═══ Middle Left ═══
            FloatingEmoji("🛒", 25.dp, 280.dp, 0.10f, floatOffset = 4f),
            FloatingEmoji("📶", 60.dp, 360.dp, 0.08f, floatOffset = 4.5f),
            FloatingEmoji("👥", 30.dp, 440.dp, 0.12f, floatOffset = 5f),
            
            // ═══ Middle Right ═══
            FloatingEmoji("🍺", 310.dp, 300.dp, 0.10f, floatOffset = 5.5f),
            FloatingEmoji("🍕", 280.dp, 380.dp, 0.08f, floatOffset = 6f),
            FloatingEmoji("🛡️", 320.dp, 460.dp, 0.12f, floatOffset = 6.5f),
            
            // ═══ Bottom Left Corner ═══
            FloatingEmoji("📺", 30.dp, 560.dp, 0.12f, floatOffset = 7f),
            FloatingEmoji("🧹", 80.dp, 620.dp, 0.10f, floatOffset = 7.5f),
            FloatingEmoji("🪴", 25.dp, 700.dp, 0.15f, floatOffset = 8f),
            FloatingEmoji("💡", 90.dp, 760.dp, 0.08f, floatOffset = 8.5f),
            
            // ═══ Bottom Right Corner ═══
            FloatingEmoji("🔌", 300.dp, 580.dp, 0.10f, floatOffset = 9f),
            FloatingEmoji("🛁", 260.dp, 640.dp, 0.12f, floatOffset = 9.5f),
            FloatingEmoji("📆", 310.dp, 720.dp, 0.15f, floatOffset = 10f),
            FloatingEmoji("🎉", 270.dp, 780.dp, 0.08f, floatOffset = 10.5f),
        )
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Decorative border frame (faint)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .border(1.dp, Color(0xFF1E3A5F).copy(alpha = 0.4f), RoundedCornerShape(24.dp))
        )

        // Floating animated background emojis
        emojis.forEach { emoji ->
            val phase = emoji.floatOffset
            val yOffset = sin((floatProgress * 2 * Math.PI + phase).toFloat()) * 15f
            val xOffset = sin((floatProgress * 2 * Math.PI + phase + 1).toFloat()) * 8f
            val rotation = sin((floatProgress * 2 * Math.PI + phase).toFloat()) * 10f
            val alphaAnim = 0.7f + sin((floatProgress * 2 * Math.PI + phase).toFloat()) * 0.3f
            
            Text(
                emoji.emoji,
                fontSize = 28.sp,
                modifier = Modifier
                    .offset(x = emoji.x + xOffset.dp, y = emoji.y + yOffset.dp)
                    .graphicsLayer(
                        rotationZ = rotation,
                        alpha = emoji.alpha * alphaAnim
                    )
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Purple glow behind logo with floating animation
            Box(contentAlignment = Alignment.Center) {
                // Outer glow
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .graphicsLayer(scaleX = logoScale.value, scaleY = logoScale.value)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(SplashPurple.copy(alpha = 0.5f), Color.Transparent),
                                center = androidx.compose.ui.geometry.Offset.Unspecified,
                                radius = 300f
                            )
                        )
                )
                
                // Actual Logo Icon
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(28.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Animated Title
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.graphicsLayer(alpha = titleAlpha.value)) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = SplashPurpleLight)) { append("WG") }
                        append(" MANAGER")
                    },
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            val parts = AppStrings.tagline.split("•").map { it.trim() }
                            parts.forEachIndexed { i, part ->
                                if (i > 0) append("  •  ")
                                if (i == parts.lastIndex) withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) { append(part) }
                                else append(part)
                            }
                        },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp
                    )
                }
            }
            
            Spacer(Modifier.height(100.dp)) // Push content up slightly
        }
    }
}
