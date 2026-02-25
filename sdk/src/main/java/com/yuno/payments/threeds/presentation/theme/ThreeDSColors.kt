package com.yuno.payments.threeds.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Internal SDK colors - bank-trust blue palette
internal val ThreeDSBlue = Color(0xFF1565C0)
internal val ThreeDSBlueDark = Color(0xFF0D47A1)
internal val ThreeDSBlueLight = Color(0xFF42A5F5)
internal val ThreeDSGreen = Color(0xFF2E7D32)
internal val ThreeDSRed = Color(0xFFC62828)

internal val ThreeDSLightColorScheme = lightColorScheme(
    primary = ThreeDSBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF546E7A),
    onSecondary = Color.White,
    surface = Color(0xFFFAFAFA),
    onSurface = Color(0xFF1C1C1C),
    background = Color.White,
    onBackground = Color(0xFF1C1C1C),
    error = ThreeDSRed,
    onError = Color.White
)

internal val ThreeDSDarkColorScheme = darkColorScheme(
    primary = ThreeDSBlueLight,
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFF90A4AE),
    onSecondary = Color.Black,
    surface = Color(0xFF1C1C1C),
    onSurface = Color(0xFFFAFAFA),
    background = Color(0xFF121212),
    onBackground = Color(0xFFFAFAFA),
    error = Color(0xFFEF5350),
    onError = Color.Black
)
