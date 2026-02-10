package com.example.wgmanager.ui.theme

import androidx.compose.ui.graphics.Color

// ── Theme Palettes (INDIGO / EMERALD / ROSE / AMBER / SKY) ─────
data class ThemePalette(
    val primary: Color,
    val primaryDark: Color,
    val primaryLight: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val accent: Color
)

val IndigoPalette = ThemePalette(
    primary = Color(0xFF6366F1), primaryDark = Color(0xFF4F46E5), primaryLight = Color(0xFFEEF2FF),
    gradientStart = Color(0xFF6366F1), gradientEnd = Color(0xFFA78BFA), accent = Color(0xFF818CF8)
)
val EmeraldPalette = ThemePalette(
    primary = Color(0xFF10B981), primaryDark = Color(0xFF059669), primaryLight = Color(0xFFECFDF5),
    gradientStart = Color(0xFF10B981), gradientEnd = Color(0xFF34D399), accent = Color(0xFF6EE7B7)
)
val RosePalette = ThemePalette(
    primary = Color(0xFFF43F5E), primaryDark = Color(0xFFE11D48), primaryLight = Color(0xFFFFF1F2),
    gradientStart = Color(0xFFF43F5E), gradientEnd = Color(0xFFFB7185), accent = Color(0xFFFDA4AF)
)
val AmberPalette = ThemePalette(
    primary = Color(0xFFF59E0B), primaryDark = Color(0xFFD97706), primaryLight = Color(0xFFFFFBEB),
    gradientStart = Color(0xFFF59E0B), gradientEnd = Color(0xFFFBBF24), accent = Color(0xFFFCD34D)
)
val SkyPalette = ThemePalette(
    primary = Color(0xFF0EA5E9), primaryDark = Color(0xFF0284C7), primaryLight = Color(0xFFF0F9FF),
    gradientStart = Color(0xFF0EA5E9), gradientEnd = Color(0xFF38BDF8), accent = Color(0xFF7DD3FC)
)

// ── Shared status colors ───────────────────────────────────────
val WGSuccess = Color(0xFF10B981)
val WGWarning = Color(0xFFF59E0B)
val WGDanger = Color(0xFFEF4444)
val WGInfo = Color(0xFF3B82F6)

// ── Surface colors ─────────────────────────────────────────────
val LightBg = Color(0xFFF8FAFC)
val LightSurface = Color.White
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightOnSurface = Color(0xFF0F172A)
val LightOnSurfaceVariant = Color(0xFF64748B)

val DarkBg = Color(0xFF0F172A)
val DarkSurface = Color(0xFF1E293B)
val DarkSurfaceVariant = Color(0xFF334155)
val DarkOnSurface = Color(0xFFF1F5F9)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)

// ── Sticky note ────────────────────────────────────────────────
val StickyYellow = Color(0xFFFEF9C3)
val StickyBorder = Color(0xFFFDE68A)
