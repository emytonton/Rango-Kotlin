package com.rango.delivery

import androidx.compose.ui.window.ComposeUIViewController
import com.rango.delivery.data.connection.IosConnectivityMonitor
import com.rango.delivery.data.push.IosLocalNotifier
import com.rango.delivery.data.storage.KeychainSecureStorage
import com.rango.delivery.di.AppContainer

private val container by lazy {
    AppContainer(
        secureStorage = KeychainSecureStorage(),
        localNotifier = IosLocalNotifier(),
        connectivityMonitor = IosConnectivityMonitor(),
    )
}

fun MainViewController() = ComposeUIViewController { App(container) }
