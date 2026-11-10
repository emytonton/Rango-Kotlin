package com.rango.delivery

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.rango.delivery.core.designsystem.component.LocalToast
import com.rango.delivery.core.designsystem.component.RangoToastHost
import com.rango.delivery.core.designsystem.component.rememberToastState
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode
import com.rango.delivery.data.theme.ThemeKeys
import com.rango.delivery.di.AppContainer
import com.rango.delivery.navigation.RangoNavHost

fun themeModeOf(theme: String): ThemeMode = when (theme) {
    ThemeKeys.LIGHT -> ThemeMode.Light
    ThemeKeys.DARK -> ThemeMode.Dark
    else -> ThemeMode.System
}

@Composable
fun isAppInDarkTheme(theme: String): Boolean = when (themeModeOf(theme)) {
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
    ThemeMode.System -> isSystemInDarkTheme()
}

@Composable
fun App(container: AppContainer) {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .crossfade(true)
            .build()
    }
    val theme by container.themeRepository.theme.collectAsState()
    RangoTheme(mode = themeModeOf(theme)) {
        val toast = rememberToastState()
        CompositionLocalProvider(LocalToast provides toast) {
            Box(Modifier.fillMaxSize()) {
                RangoNavHost(container)
                RangoToastHost(toast)
            }
        }
    }
}
