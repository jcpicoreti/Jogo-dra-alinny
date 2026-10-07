package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DentalColorScheme = darkColorScheme(
    primary = DentalCyan,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0369A1),
    onPrimaryContainer = Color.White,
    secondary = DentalTeal,
    onSecondary = Color.White,
    tertiary = DentalGold,
    onTertiary = Color(0xFF0F172A),
    background = DentalBlueDark,
    onBackground = Color.White,
    surface = DentalSurfaceDark,
    onSurface = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DentalColorScheme,
        typography = Typography,
        content = content
    )
}
