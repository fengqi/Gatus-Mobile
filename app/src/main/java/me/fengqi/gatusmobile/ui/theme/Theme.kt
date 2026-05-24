package me.fengqi.gatusmobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GatusColorScheme = darkColorScheme(
    primary = GatusPrimary,
    onPrimary = GatusTextPrimary,
    secondary = GatusAccent,
    onSecondary = GatusTextPrimary,
    background = GatusBackground,
    onBackground = GatusTextPrimary,
    surface = GatusCard,
    onSurface = GatusTextPrimary,
    surfaceVariant = GatusCardBorder,
    onSurfaceVariant = GatusTextSecondary,
    outline = GatusCardBorder,
    error = GatusUnhealthy,
    onError = GatusTextPrimary
)

@Composable
fun GatusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GatusColorScheme,
        typography = Typography,
        content = content
    )
}
