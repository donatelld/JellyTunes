package com.jellytunes.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * JellyTunes color scheme definition
 */
@Immutable
data class JellyTunesColors(
    val name: String,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val primaryLight: Color,
    val primaryDark: Color,
    val accent: Color,
    val gradientOverlayEnd: Color,
    val coverGradientStart: Color,
    val coverGradientMid: Color,
    val coverGradientEnd: Color,
    val progressTrack: Color,
    val progressIndicator: Color,
    val progressGlow: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color
)

/**
 * Available themes
 */
enum class ThemeType {
    AMBER,
    FOREST,
    SUNSET,
    BURGUNDY
}

object JellyTunesThemes {
    
    val Amber = JellyTunesColors(
        name = "Amber",
        background = Color(0xFF0C0A08),
        surface = Color(0xFF1A1714),
        surfaceVariant = Color(0xFF252220),
        primary = Color(0xFFD4A574),
        primaryLight = Color(0xFFE8C49A),
        primaryDark = Color(0xFFB8956A),
        accent = Color(0xFFE07B67),
        gradientOverlayEnd = Color(0xFF0C0A08),
        coverGradientStart = Color(0xFF2D2520),
        coverGradientMid = Color(0xFF3D322A),
        coverGradientEnd = Color(0xFF4A3C32),
        progressTrack = Color(0xFF2A2622),
        progressIndicator = Color(0xFFD4A574),
        progressGlow = Color(0x40D4A574),
        textPrimary = Color(0xFFFAF8F5),
        textSecondary = Color(0xFFB8B0A8),
        textMuted = Color(0xFF7A7570)
    )
    
    val Forest = JellyTunesColors(
        name = "Forest",
        background = Color(0xFF080C08),
        surface = Color(0xFF141A14),
        surfaceVariant = Color(0xFF202520),
        primary = Color(0xFF6BCB77),
        primaryLight = Color(0xFF8FD99A),
        primaryDark = Color(0xFF4CAF50),
        accent = Color(0xFF81C784),
        gradientOverlayEnd = Color(0xFF080C08),
        coverGradientStart = Color(0xFF1A2E1A),
        coverGradientMid = Color(0xFF243D24),
        coverGradientEnd = Color(0xFF2E4A2E),
        progressTrack = Color(0xFF222A22),
        progressIndicator = Color(0xFF6BCB77),
        progressGlow = Color(0x406BCB77),
        textPrimary = Color(0xFFF5FAF5),
        textSecondary = Color(0xFFA8B8A8),
        textMuted = Color(0xFF707A70)
    )
    
    val Sunset = JellyTunesColors(
        name = "Sunset",
        background = Color(0xFF0C0908),
        surface = Color(0xFF1A1514),
        surfaceVariant = Color(0xFF252020),
        primary = Color(0xFFFF8A65),
        primaryLight = Color(0xFFFFAB91),
        primaryDark = Color(0xFFE57350),
        accent = Color(0xFFFFB74D),
        gradientOverlayEnd = Color(0xFF0C0908),
        coverGradientStart = Color(0xFF2D1F1A),
        coverGradientMid = Color(0xFF3D2A22),
        coverGradientEnd = Color(0xFF4A332A),
        progressTrack = Color(0xFF2A2220),
        progressIndicator = Color(0xFFFF8A65),
        progressGlow = Color(0x40FF8A65),
        textPrimary = Color(0xFFFAF7F5),
        textSecondary = Color(0xFFB8ADA8),
        textMuted = Color(0xFF7A7270)
    )
    
    val Burgundy = JellyTunesColors(
        name = "Burgundy",
        background = Color(0xFF0A0808),
        surface = Color(0xFF1A1414),
        surfaceVariant = Color(0xFF251E1E),
        primary = Color(0xFFCD5C5C),
        primaryLight = Color(0xFFE08080),
        primaryDark = Color(0xFFB84A4A),
        accent = Color(0xFFDC7C7C),
        gradientOverlayEnd = Color(0xFF0A0808),
        coverGradientStart = Color(0xFF2A1A1A),
        coverGradientMid = Color(0xFF3A2424),
        coverGradientEnd = Color(0xFF4A2E2E),
        progressTrack = Color(0xFF2A2020),
        progressIndicator = Color(0xFFCD5C5C),
        progressGlow = Color(0x40CD5C5C),
        textPrimary = Color(0xFFFAF5F5),
        textSecondary = Color(0xFFB8A8A8),
        textMuted = Color(0xFF7A7070)
    )
    
    fun getTheme(type: ThemeType): JellyTunesColors {
        return when (type) {
            ThemeType.AMBER -> Amber
            ThemeType.FOREST -> Forest
            ThemeType.SUNSET -> Sunset
            ThemeType.BURGUNDY -> Burgundy
        }
    }
    
    fun getNextTheme(current: ThemeType): ThemeType {
        val values = ThemeType.entries
        val currentIndex = values.indexOf(current)
        return values[(currentIndex + 1) % values.size]
    }
    
    fun getPreviousTheme(current: ThemeType): ThemeType {
        val values = ThemeType.entries
        val currentIndex = values.indexOf(current)
        return values[(currentIndex - 1 + values.size) % values.size]
    }
}

val LocalJellyTunesColors = staticCompositionLocalOf { JellyTunesThemes.Amber }
