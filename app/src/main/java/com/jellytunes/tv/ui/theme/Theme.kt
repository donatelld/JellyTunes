package com.jellytunes.tv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun JellyTunesTheme(
    colors: JellyTunesColors = JellyTunesThemes.Amber,
    content: @Composable () -> Unit
) {
    val materialColorScheme = darkColorScheme(
        primary = colors.primary,
        onPrimary = colors.textPrimary,
        primaryContainer = colors.primaryDark,
        onPrimaryContainer = colors.textPrimary,
        secondary = colors.primaryLight,
        onSecondary = colors.background,
        tertiary = colors.accent,
        onTertiary = colors.background,
        background = colors.background,
        onBackground = colors.textPrimary,
        surface = colors.surface,
        onSurface = colors.textPrimary,
        surfaceVariant = colors.surfaceVariant,
        onSurfaceVariant = colors.textSecondary
    )

    CompositionLocalProvider(
        LocalJellyTunesColors provides colors
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            content = content
        )
    }
}
