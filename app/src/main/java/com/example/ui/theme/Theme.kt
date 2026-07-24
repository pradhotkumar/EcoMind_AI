package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EcoDarkColorScheme = darkColorScheme(
    primary = ElectricEmerald,
    secondary = NeonCyan,
    tertiary = AccentGold,
    background = DarkBg,
    surface = DarkSurface,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    primaryContainer = SoftGlassOverlay,
    onPrimaryContainer = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = StrokeHighlight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force premium dark mode by default
    dynamicColor: Boolean = false, // Disable dynamic colors to keep elite dark glassmorphic green/cyan vibe
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = EcoDarkColorScheme,
        typography = Typography,
        content = content
    )
}
