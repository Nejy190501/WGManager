package com.example.wgmanager.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.wgmanager.data.ThemeColor

// ── CompositionLocal for the active palette ────────────────────
val LocalThemePalette = staticCompositionLocalOf { IndigoPalette }

@Composable
fun WGManagerTheme(
    themeColor: ThemeColor = ThemeColor.INDIGO,
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val palette = when (themeColor) {
        ThemeColor.INDIGO -> IndigoPalette
        ThemeColor.EMERALD -> EmeraldPalette
        ThemeColor.ROSE -> RosePalette
        ThemeColor.AMBER -> AmberPalette
        ThemeColor.SKY -> SkyPalette
    }

    val colorScheme = if (darkTheme) darkColorScheme(
        primary = palette.primary, onPrimary = Color.White,
        primaryContainer = palette.primaryDark, onPrimaryContainer = palette.primaryLight,
        secondary = palette.accent, secondaryContainer = palette.primaryLight,
        background = DarkBg, onBackground = DarkOnSurface,
        surface = DarkSurface, onSurface = DarkOnSurface,
        surfaceVariant = DarkSurfaceVariant, onSurfaceVariant = DarkOnSurfaceVariant,
        error = WGDanger, outline = Color(0xFF475569), outlineVariant = Color(0xFF334155),
    ) else lightColorScheme(
        primary = palette.primary, onPrimary = Color.White,
        primaryContainer = palette.primaryLight, onPrimaryContainer = palette.primaryDark,
        secondary = palette.accent, secondaryContainer = palette.primaryLight,
        background = LightBg, onBackground = LightOnSurface,
        surface = LightSurface, onSurface = LightOnSurface,
        surfaceVariant = LightSurfaceVariant, onSurfaceVariant = LightOnSurfaceVariant,
        error = WGDanger, outline = Color(0xFFCBD5E1), outlineVariant = Color(0xFFE2E8F0),
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalThemePalette provides palette) {
        MaterialTheme(colorScheme = colorScheme, typography = WGTypography, content = content)
    }
}
