package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SilentRushColorScheme = darkColorScheme(
    primary = GlowBlue,
    onPrimary = StarWhite,
    primaryContainer = GlassSurface,
    onPrimaryContainer = StarWhite,
    secondary = GlowCaelum,
    onSecondary = StarWhite,
    tertiary = NebulaViolet,
    onTertiary = StarWhite,
    background = DeepBlack,
    onBackground = StarWhite,
    surface = MidnightBlue,
    onSurface = StarWhite,
    surfaceVariant = Color(0x0EFFFFFF),
    onSurfaceVariant = CosmicGray,
    outline = GlassBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SilentRushColorScheme,
        typography = Typography,
        content = content
    )
}
