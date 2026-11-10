package com.rango.delivery.data.push

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberNotificationPermission(): NotificationPermission = remember { IosNotificationPermission() }

private class IosNotificationPermission : NotificationPermission {
    private val center = UNUserNotificationCenter.currentNotificationCenter()

    override var granted by mutableStateOf(true)
        private set

    private var undetermined = false

    init {
        refresh()
    }

    override fun refresh() {
        center.getNotificationSettingsWithCompletionHandler { settings ->
            val status = settings?.authorizationStatus
            dispatch_async(dispatch_get_main_queue()) {
                granted = status == UNAuthorizationStatusAuthorized || status == UNAuthorizationStatusProvisional
                undetermined = status == UNAuthorizationStatusNotDetermined
            }
        }
    }

    override fun request() {
        if (undetermined) {
            center.requestAuthorizationWithOptions(
                UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
            ) { _, _ -> refresh() }
        } else {
            val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
            UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any>(), null)
        }
    }
}
