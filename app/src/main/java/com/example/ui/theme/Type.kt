package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// We use the high-quality pre-installed Noto Sans Arabic and Noto Naskh Arabic (Serif) on Android devices
val QuranFontFamily = FontFamily.Serif // Renders gorgeous traditional Naskh style for Holly Arabic verses
val UiFontFamily = FontFamily.SansSerif  // Renders beautiful, clean modern Arabic typeface for labels

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    displayMedium = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = QuranFontFamily, // Traditional Naskh Arabic text reading
        fontWeight = FontWeight.Normal,
        fontSize = 25.sp, // Raised font size for comfortable reading of Arabic diacritics
        lineHeight = 44.sp, // Ample line height to keep Arabic vowels (dhamma, fatha, kasra) visible and clear
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    labelMedium = TextStyle(
        fontFamily = UiFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 18.sp
    )
)


