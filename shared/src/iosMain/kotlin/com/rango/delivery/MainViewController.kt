package com.rango.delivery

import androidx.compose.ui.window.ComposeUIViewController
import com.rango.delivery.data.storage.KeychainSecureStorage
import com.rango.delivery.di.AppContainer

private val container by lazy { AppContainer(secureStorage = KeychainSecureStorage()) }

fun MainViewController() = ComposeUIViewController { App(container) }
