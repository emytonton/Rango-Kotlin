package com.rango.delivery.core.connectivity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue

val LocalOnline = compositionLocalOf { true }

@Composable
fun OnReconnect(action: () -> Unit) {
    val online = LocalOnline.current
    val currentAction by rememberUpdatedState(action)
    var wasOffline by remember { mutableStateOf(!online) }
    LaunchedEffect(online) {
        if (online && wasOffline) currentAction()
        wasOffline = !online
    }
}
