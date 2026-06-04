package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = NavyPrimary,
    secondary = GoldDark,
    background = NavyPrimary,
    surface = NavyCard,
    onBackground = CreamText,
    onSurface = CreamText,
    primaryContainer = NavyAccent,
    onPrimaryContainer = CreamText
)

private val LightColorScheme = lightColorScheme(
    primary = GoldPrimary,
    onPrimary = NavyPrimary,
    secondary = GoldDark,
    background = NavyPrimary, // Keep Islamic theme default dark for luxury look as requested!
    surface = NavyCard,
    onBackground = CreamText,
    onSurface = CreamText,
    primaryContainer = NavyAccent,
    onPrimaryContainer = CreamText
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Keep it always in Dark Mode or dark palette styled by default for premium feel
    val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

