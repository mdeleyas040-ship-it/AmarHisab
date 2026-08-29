package com.eleyas.expensetracker.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

val Green = Color(0xFF0B8F45)
val DarkGreen = Color(0xFF101217) // Matches MainActivity
val AccentGreen = Color(0xFF00E676)
val LightGreen = Color(0xFFEAF8EF)
val IncomeGreen = Color(0xFF168A45)
val ExpenseRed = Color(0xFFD32F2F)
val Blue = Color(0xFF1976D2)

// Light Mode
val LightColorScheme = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,

    secondary = DarkGreen,
    onSecondary = Color.White,

    tertiary = Green,

    background = Color(0xFFF7F9F7),
    onBackground = Color(0xFF111111),

    surface = Color.White,
    onSurface = Color(0xFF111111),

    surfaceVariant = Color(0xFFE8EFEA),
    onSurfaceVariant = Color(0xFF37423A)
)

// Dark Mode - High Contrast
val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF5DDB8A),
    onPrimary = Color(0xFF00210C),

    secondary = Color(0xFF7FE3A0),
    onSecondary = Color(0xFF00210C),

    tertiary = Color(0xFF5DDB8A),

    background = Color(0xFF101512),
    onBackground = Color(0xFFF5F7F5),

    surface = Color(0xFF171D19),
    onSurface = Color(0xFFF5F7F5),

    surfaceVariant = Color(0xFF252D28),
    onSurfaceVariant = Color(0xFFD5DDD7),

    outline = Color(0xFF9AA69E)
)