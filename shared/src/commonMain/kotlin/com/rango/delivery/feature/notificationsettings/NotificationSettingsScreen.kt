package com.rango.delivery.feature.notificationsettings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rango.delivery.core.designsystem.component.LocalToast
import com.rango.delivery.core.designsystem.component.RangoDashedButton
import com.rango.delivery.core.designsystem.component.RangoDashedNote
import com.rango.delivery.core.designsystem.component.RangoErrorState
import com.rango.delivery.core.designsystem.component.RangoSettingsRow
import com.rango.delivery.core.designsystem.component.RangoSkeleton
import com.rango.delivery.core.designsystem.component.RangoTopBar
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode
import com.rango.delivery.data.profile.NotificationPrefsDto
import com.rango.delivery.data.push.rememberNotificationPermission
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.notification_settings_denied
import com.rango.delivery.resources.notification_settings_granted
import com.rango.delivery.resources.notification_settings_news
import com.rango.delivery.resources.notification_settings_news_hint
import com.rango.delivery.resources.notification_settings_orders
import com.rango.delivery.resources.notification_settings_orders_hint
import com.rango.delivery.resources.notification_settings_promos
import com.rango.delivery.resources.notification_settings_promos_hint
import com.rango.delivery.resources.notification_settings_test
import com.rango.delivery.resources.notifications_retry
import com.rango.delivery.resources.notifications_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private data class PrefOption(
    val pref: NotificationPref,
    val label: StringResource,
    val hint: StringResource,
    val icon: ImageVector,
)

private val Options = listOf(
    PrefOption(NotificationPref.Orders, Res.string.notification_settings_orders, Res.string.notification_settings_orders_hint, RangoIcons.ReceiptLong),
    PrefOption(NotificationPref.Promos, Res.string.notification_settings_promos, Res.string.notification_settings_promos_hint, RangoIcons.LocalOffer),
    PrefOption(NotificationPref.News, Res.string.notification_settings_news, Res.string.notification_settings_news_hint, RangoIcons.Campaign),
)

private val GroupShape = RoundedCornerShape(20.dp)

data class NotificationSettingsActions(
    val onBack: () -> Unit = {},
    val onToggle: (NotificationPref) -> Unit = {},
    val onTestPush: () -> Unit = {},
    val onRequestPermission: () -> Unit = {},
    val onRetry: () -> Unit = {},
)

@Composable
fun NotificationSettingsRoute(
    viewModel: NotificationSettingsViewModel,
    onBack: () -> Unit,
    onSessionExpired: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val permission = rememberNotificationPermission()
    val toast = LocalToast.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is NotificationSettingsEvent.ShowToast -> toast.show(event.message, event.kind)
                NotificationSettingsEvent.SessionExpired -> onSessionExpired()
            }
        }
    }

    LifecycleResumeEffect(permission) {
        permission.refresh()
        onPauseOrDispose { }
    }

    NotificationSettingsScreen(
        state = state,
        permissionGranted = permission.granted,
        actions = NotificationSettingsActions(
            onBack = onBack,
            onToggle = viewModel::onToggle,
            onTestPush = { if (permission.granted) viewModel.sendTest() else permission.request() },
            onRequestPermission = permission::request,
            onRetry = viewModel::retry,
        ),
    )
}

@Composable
fun NotificationSettingsScreen(
    state: NotificationSettingsUiState,
    permissionGranted: Boolean,
    actions: NotificationSettingsActions,
) {
    val colors = Rango.colors
    Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
        RangoTopBar(title = stringResource(Res.string.notifications_title), onBack = actions.onBack)
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 22.dp),
        ) {
            when {
                state.prefs != null -> PrefsGroup(state, actions.onToggle)
                state.errorMessage != null -> RangoErrorState(
                    message = state.errorMessage,
                    actionText = stringResource(Res.string.notifications_retry),
                    onAction = actions.onRetry,
                )
                else -> RangoSkeleton(Modifier.fillMaxWidth().height(195.dp), cornerRadius = 20.dp)
            }
            RangoDashedButton(
                text = stringResource(Res.string.notification_settings_test),
                icon = RangoIcons.NotificationsActive,
                onClick = actions.onTestPush,
                accent = true,
            )
            if (permissionGranted) {
                RangoDashedNote(
                    icon = RangoIcons.VerifiedUser,
                    text = stringResource(Res.string.notification_settings_granted),
                    iconTint = colors.success,
                )
            } else {
                RangoDashedNote(
                    icon = RangoIcons.NotificationsOff,
                    text = stringResource(Res.string.notification_settings_denied),
                    iconTint = colors.error,
                    onClick = actions.onRequestPermission,
                )
            }
        }
    }
}

@Composable
private fun PrefsGroup(state: NotificationSettingsUiState, onToggle: (NotificationPref) -> Unit) {
    val colors = Rango.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(GroupShape)
            .background(colors.surface)
            .border(1.dp, colors.border, GroupShape),
    ) {
        Options.forEachIndexed { index, option ->
            RangoSettingsRow(
                icon = option.icon,
                label = stringResource(option.label),
                hint = stringResource(option.hint),
                switchChecked = state.isOn(option.pref),
                showChevron = false,
                showDivider = index > 0,
                largeHint = true,
                onClick = { onToggle(option.pref) },
            )
        }
    }
}

private val PreviewState = NotificationSettingsUiState(
    loading = false,
    prefs = NotificationPrefsDto(orders = true, promos = true, news = false),
)

@Preview
@Composable
private fun NotificationSettingsScreenPreview() {
    RangoTheme(ThemeMode.Light) {
        NotificationSettingsScreen(PreviewState, permissionGranted = true, actions = NotificationSettingsActions())
    }
}

@Preview
@Composable
private fun NotificationSettingsScreenDarkPreview() {
    RangoTheme(ThemeMode.Dark) {
        NotificationSettingsScreen(PreviewState, permissionGranted = false, actions = NotificationSettingsActions())
    }
}
