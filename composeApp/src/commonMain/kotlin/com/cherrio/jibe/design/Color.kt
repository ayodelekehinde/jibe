package com.cherrio.jibe.design

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Light Theme Colors
val JibeBlue = Color(0xFF4A80F0)
val JibeLightBackground = Color(0xFFF9F9FF)
val JibeLightSurface = Color.White
val JibeOnSurface = Color(0xFF1C1B1F)
val JibeConnectedGreen = Color(0xFF34C759)
val JibeOfflineGray = Color.Gray

// Dark Theme Colors
val JibeDarkBlue = Color(0xFFADC6FF)
val JibeDarkBackground = Color(0xFF121318)
val JibeDarkSurface = Color(0xFF22242C)
val JibeOnSurfaceDark = Color(0xFFE3E2E6)


val LightColorScheme = lightColorScheme(
    primary = JibeBlue,
    onPrimary = Color.White,
    background = JibeLightBackground,
    onBackground = JibeOnSurface,
    surface = JibeLightSurface,
    onSurface = JibeOnSurface,
    surfaceVariant = JibeLightBackground, // For cards
    onSurfaceVariant = JibeOnSurface
)

val DarkColorScheme = darkColorScheme(
    primary = JibeDarkBlue,
    onPrimary = Color.Black,
    background = JibeDarkBackground,
    onBackground = JibeOnSurfaceDark,
    surface = JibeDarkSurface,
    onSurface = JibeOnSurfaceDark,
    surfaceVariant = JibeDarkSurface, // For cards
    onSurfaceVariant = JibeOnSurfaceDark
)