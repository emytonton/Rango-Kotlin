package com.rango.delivery.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.plus_jakarta_sans_bold
import com.rango.delivery.resources.plus_jakarta_sans_extrabold
import com.rango.delivery.resources.plus_jakarta_sans_medium
import com.rango.delivery.resources.plus_jakarta_sans_semibold
import org.jetbrains.compose.resources.Font

@Immutable
data class RangoTypography(
    val display: TextStyle,
    val title: TextStyle,
    val section: TextStyle,
    val bodyLarge: TextStyle,
    val body: TextStyle,
    val label: TextStyle,
    val caption: TextStyle,
    val button: TextStyle,
    val mono: TextStyle,
)

@Composable
fun plusJakartaSans(): FontFamily = FontFamily(
    Font(Res.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(Res.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(Res.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(Res.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),
)

fun rangoTypography(family: FontFamily) = RangoTypography(
    display = TextStyle(fontFamily = family, fontWeight = FontWeight.ExtraBold, fontSize = 31.sp, lineHeight = 1.14.em, letterSpacing = (-1.1).sp),
    title = TextStyle(fontFamily = family, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp, letterSpacing = (-0.6).sp),
    section = TextStyle(fontFamily = family, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, letterSpacing = (-0.4).sp),
    bodyLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 14.5.sp, lineHeight = 1.5.em),
    body = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    label = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
    caption = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 1.5.em),
    button = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 16.sp),
    mono = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.5.sp),
)

val LocalRangoTypography = staticCompositionLocalOf { rangoTypography(FontFamily.Default) }
