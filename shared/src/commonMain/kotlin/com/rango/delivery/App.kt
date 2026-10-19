package com.rango.delivery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.rango.delivery.core.designsystem.component.LocalToast
import com.rango.delivery.core.designsystem.component.RangoToastHost
import com.rango.delivery.core.designsystem.component.rememberToastState
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.di.AppContainer
import com.rango.delivery.navigation.RangoNavHost

@Composable
fun App(container: AppContainer) {
    RangoTheme {
        val toast = rememberToastState()
        CompositionLocalProvider(LocalToast provides toast) {
            Box(Modifier.fillMaxSize()) {
                RangoNavHost(container)
                RangoToastHost(toast)
            }
        }
    }
}
