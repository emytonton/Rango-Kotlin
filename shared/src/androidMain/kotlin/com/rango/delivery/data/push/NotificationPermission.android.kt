package com.rango.delivery.data.push

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

@Composable
actual fun rememberNotificationPermission(): NotificationPermission {
    val context = LocalContext.current
    val state = remember { AndroidNotificationPermission(context) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        state.onResult(granted)
    }
    state.launcher = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
    return state
}

private class AndroidNotificationPermission(private val context: Context) : NotificationPermission {
    override var granted by mutableStateOf(check())
        private set

    var launcher: () -> Unit = {}
    private var requestedAt = 0L

    override fun refresh() {
        granted = check()
    }

    override fun request() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !runtimeGranted()) {
            requestedAt = System.currentTimeMillis()
            launcher()
        } else {
            openSettings()
        }
    }

    fun onResult(result: Boolean) {
        refresh()
        // Resposta imediata indica que o sistema não mostrou o diálogo (permissão negada de vez).
        if (!result && System.currentTimeMillis() - requestedAt < DIALOG_SKIPPED_MS) openSettings()
    }

    private fun check(): Boolean = runtimeGranted() && NotificationManagerCompat.from(context).areNotificationsEnabled()

    private fun runtimeGranted(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private companion object {
        const val DIALOG_SKIPPED_MS = 400L
    }
}
