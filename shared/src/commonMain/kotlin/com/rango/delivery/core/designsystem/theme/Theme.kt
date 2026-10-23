package com.rango.delivery.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember

enum class ThemeMode { Light, Dark, System }

@Composable
fun RangoTheme(
    mode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit,
) {
    val dark = when (mode) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System -> isSystemInDarkTheme()
    }
    val colors = if (dark) DarkRangoColors else LightRangoColors
    val family = plusJakartaSans()
    val typography = remember(family) { rangoTypography(family) }
    val scheme = if (dark) {
        darkColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            background = colors.background,
            onBackground = colors.text,
            surface = colors.surface,
            onSurface = colors.text,
            error = colors.error,
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            background = colors.background,
            onBackground = colors.text,
            surface = colors.surface,
            onSurface = colors.text,
            error = colors.error,
        )
    }

    CompositionLocalProvider(
        LocalRangoColors provides colors,
        LocalRangoTypography provides typography,
    ) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}

object Rango {
    val colors: RangoColors
        @Composable @ReadOnlyComposable get() = LocalRangoColors.current

    val typography: RangoTypography
        @Composable @ReadOnlyComposable get() = LocalRangoTypography.current
}
