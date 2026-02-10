package com.example.wgmanager.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onNavigate: (AppScreen) -> Unit,
    toast: ToastState
) {
    val s = AppStrings
    val user = DataStore.currentUser ?: return
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    // Floating animation for emojis
    val infiniteTransition = rememberInfiniteTransition(label = "onboarding")
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = EaseInOutCubic),
            RepeatMode.Reverse
        ),
        label = "floatY"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            tween(1800, easing = EaseInOutCubic),
            RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surface)
                )
            )
    ) {
        // Subtle border card
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color.Transparent,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Pager content
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        when (page) {
                            // ── Page 1: Willkommen zuhause! ──
                            0 -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 40.dp)
                                ) {
                                    Text(
                                        "👋",
                                        fontSize = 56.sp,
                                        modifier = Modifier
                                            .offset(y = floatY.dp)
                                            .scale(pulseScale)
                                    )
                                    Spacer(Modifier.height(24.dp))
                                    Text(
                                        s.welcomeHome,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        s.welcomeHomeDesc,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 15.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 22.sp
                                    )
                                }
                            }

                            // ── Page 2: Hausregeln ──
                            1 -> {
                                val rules = DataStore.getWGRules()
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 40.dp)
                                ) {
                                    Text(
                                        "📜",
                                        fontSize = 56.sp,
                                        modifier = Modifier
                                            .offset(y = floatY.dp)
                                            .scale(pulseScale)
                                    )
                                    Spacer(Modifier.height(24.dp))
                                    Text(
                                        s.hausregeln,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    if (rules.isNotBlank()) {
                                        Text(
                                            rules,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 22.sp
                                        )
                                    } else {
                                        Text(
                                            s.noRulesYet,
                                            color = MaterialTheme.colorScheme.outline,
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            // ── Page 3: Bereit? ──
                            2 -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 40.dp)
                                ) {
                                    Text(
                                        "🚀",
                                        fontSize = 56.sp,
                                        modifier = Modifier
                                            .offset(y = floatY.dp)
                                            .scale(pulseScale)
                                    )
                                    Spacer(Modifier.height(24.dp))
                                    Text(
                                        s.readyTitle,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        s.readyDesc,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 15.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Page indicator dots ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { idx ->
                        val isActive = pagerState.currentPage == idx
                        Surface(
                            shape = CircleShape,
                            color = if (isActive) Color(0xFF3B82F6) else MaterialTheme.colorScheme.outline,
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isActive) 10.dp else 8.dp)
                        ) {}
                    }
                }

                // ── CTA Button ──
                Button(
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            // Complete onboarding
                            DataStore.initOnboarding(user)
                            user.onboardingSteps.forEach { step ->
                                DataStore.completeOnboardingStep(step.type)
                            }
                            user.onboardingCompleted = true
                            DataStore.syncUser(user)
                            toast.show(s.onboardingComplete)
                            onNavigate(AppScreen.DASHBOARD)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp)
                        .padding(bottom = 48.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Text(
                        if (pagerState.currentPage < 2) "${s.weiter}  →"
                        else "${s.losGehts}  →",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}