package com.rango.delivery.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class RangoColors(
    val page: Color,
    val background: Color,
    val surface: Color,
    val surface2: Color,
    val text: Color,
    val text2: Color,
    val border: Color,
    val primary: Color,
    val primary2: Color,
    val onPrimary: Color,
    val soft: Color,
    val success: Color,
    val error: Color,
    val skeleton: Color,
    val shadow: Color,
    val isDark: Boolean,
)

val LightRangoColors = RangoColors(
    page = Color(0xFFEDE9E3),
    background = Color(0xFFF7F7F7),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFF7F7F7),
    text = Color(0xFF1F1F1F),
    text2 = Color(0xFF737373),
    border = Color(0xFFE8E8E8),
    primary = Color(0xFFFF6B00),
    primary2 = Color(0xFFFF8A34),
    onPrimary = Color(0xFFFFFFFF),
    soft = Color(0xFFFFF1E6),
    success = Color(0xFF22A447),
    error = Color(0xFFE53935),
    skeleton = Color(0xFFECECEC),
    shadow = Color(0x171F1F1F),
    isDark = false,
)

val DarkRangoColors = RangoColors(
    page = Color(0xFF232326),
    background = Color(0xFF101012),
    surface = Color(0xFF17181B),
    surface2 = Color(0xFF1E2024),
    text = Color(0xFFF4F4F5),
    text2 = Color(0xFF9B9BA3),
    border = Color(0xFF2A2C31),
    primary = Color(0xFFFF6B00),
    primary2 = Color(0xFFFF8A34),
    onPrimary = Color(0xFFFFFFFF),
    soft = Color(0xFF2C1A0C),
    success = Color(0xFF31C25C),
    error = Color(0xFFFF5F58),
    skeleton = Color(0xFF23252A),
    shadow = Color(0x8C000000),
    isDark = true,
)

val LocalRangoColors = staticCompositionLocalOf { LightRangoColors }
