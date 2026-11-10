package com.rango.delivery.data.push

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
interface NotificationPermission {
    val granted: Boolean
    fun refresh()
    fun request()
}

@Composable
expect fun rememberNotificationPermission(): NotificationPermission
