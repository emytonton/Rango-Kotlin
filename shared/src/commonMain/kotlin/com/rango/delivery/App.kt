package com.rango.delivery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.rango.delivery.core.connectivity.LocalOnline
import com.rango.delivery.core.connectivity.OnReconnect
import com.rango.delivery.core.designsystem.component.LocalToast
import com.rango.delivery.core.designsystem.component.ProvideToast
import com.rango.delivery.core.designsystem.component.RangoOfflineBanner
import com.rango.delivery.core.designsystem.component.RangoPushBanner
import com.rango.delivery.core.designsystem.component.RangoToastHost
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.designsystem.component.rememberToastState
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode
import com.rango.delivery.data.favorite.FavoriteRepository
import com.rango.delivery.data.push.PushMessage
import com.rango.delivery.data.push.PushWatcher
import com.rango.delivery.data.theme.ThemeKeys
import com.rango.delivery.di.AppContainer
import com.rango.delivery.navigation.RangoNavHost
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.connection_restored
import com.rango.delivery.resources.offline_banner_text
import com.rango.delivery.resources.offline_banner_title
import com.rango.delivery.resources.push_app_name
import com.rango.delivery.resources.push_now
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

private const val PUSH_BANNER_MS = 6_500L

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
    val pushWatcher = container.pushWatcher
    LaunchedEffect(pushWatcher) { pushWatcher.start() }
    LifecycleStartEffect(pushWatcher) {
        pushWatcher.inForeground = true
        onStopOrDispose { pushWatcher.inForeground = false }
    }

    val theme by container.themeRepository.theme.collectAsState()
    val online by container.connectivityMonitor.online.collectAsState()
    RangoTheme(mode = themeModeOf(theme)) {
        val toast = rememberToastState()
        ProvideToast(toast) {
            CompositionLocalProvider(LocalOnline provides online) {
                ConnectionEffects(container)
                Box(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize()) {
                        if (!online) OfflineBanner()
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .then(if (online) Modifier else Modifier.consumeWindowInsets(WindowInsets.statusBars)),
                        ) {
                            RangoNavHost(container)
                        }
                    }
                    RangoToastHost(toast)
                    PushBannerHost(pushWatcher)
                }
            }
        }
    }
}

@Composable
private fun ConnectionEffects(container: AppContainer) {
    val toast = LocalToast.current
    val restoredMessage = stringResource(Res.string.connection_restored)
    OnReconnect {
        toast.show(restoredMessage, ToastKind.Success)
    }
    val online = LocalOnline.current
    LaunchedEffect(online) {
        if (online) container.favoriteRepository.syncQuietly()
    }
}

private suspend fun FavoriteRepository.syncQuietly() {
    try {
        syncPending()
    } catch (e: Exception) {
        if (e is CancellationException) throw e
    }
}

@Composable
private fun OfflineBanner() {
    Column(Modifier.fillMaxWidth().background(Rango.colors.surface).windowInsetsPadding(WindowInsets.statusBars)) {
        RangoOfflineBanner(
            title = stringResource(Res.string.offline_banner_title),
            text = stringResource(Res.string.offline_banner_text),
        )
    }
}

@Composable
private fun BoxScope.PushBannerHost(pushWatcher: PushWatcher) {
    val banner by pushWatcher.banner.collectAsState()
    var last by remember { mutableStateOf<PushMessage?>(null) }
    banner?.let { last = it }

    LaunchedEffect(banner) {
        if (banner != null) {
            delay(PUSH_BANNER_MS)
            pushWatcher.dismissBanner()
        }
    }

    AnimatedVisibility(
        visible = banner != null,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 11.dp, vertical = 10.dp),
    ) {
        last?.let { message ->
            RangoPushBanner(
                appName = stringResource(Res.string.push_app_name),
                time = stringResource(Res.string.push_now),
                title = message.title,
                text = message.body,
                onClick = pushWatcher::openFromBanner,
            )
        }
    }
}
