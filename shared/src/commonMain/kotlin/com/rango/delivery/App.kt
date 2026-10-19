package com.rango.delivery

import androidx.compose.runtime.Composable
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.navigation.RangoNavHost

@Composable
fun App() {
    RangoTheme {
        RangoNavHost()
    }
}
