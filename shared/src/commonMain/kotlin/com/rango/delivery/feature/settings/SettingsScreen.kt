package com.rango.delivery.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rango.delivery.core.designsystem.component.LocalToast
import com.rango.delivery.core.designsystem.component.RangoSettingsGroup
import com.rango.delivery.core.designsystem.component.RangoSettingsRow
import com.rango.delivery.core.designsystem.component.RangoTopBar
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.settings_addresses
import com.rango.delivery.resources.settings_clear
import com.rango.delivery.resources.settings_clear_hint
import com.rango.delivery.resources.settings_group_about
import com.rango.delivery.resources.settings_group_account
import com.rango.delivery.resources.settings_group_data
import com.rango.delivery.resources.settings_group_preferences
import com.rango.delivery.resources.settings_help
import com.rango.delivery.resources.settings_language
import com.rango.delivery.resources.settings_language_value
import com.rango.delivery.resources.settings_notifications
import com.rango.delivery.resources.settings_offline
import com.rango.delivery.resources.settings_payments
import com.rango.delivery.resources.settings_personal_data
import com.rango.delivery.resources.settings_sync
import com.rango.delivery.resources.settings_syncing
import com.rango.delivery.resources.settings_terms
import com.rango.delivery.resources.settings_theme
import com.rango.delivery.resources.settings_title
import com.rango.delivery.resources.settings_version
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import org.jetbrains.compose.resources.stringResource

data class SettingsNavigation(
    val onBack: () -> Unit = {},
    val onTheme: () -> Unit = {},
    val onNotifications: () -> Unit = {},
    val onPersonalData: () -> Unit = {},
    val onAddresses: () -> Unit = {},
    val onPayments: () -> Unit = {},
)

data class SettingsActions(
    val navigation: SettingsNavigation = SettingsNavigation(),
    val onLanguage: () -> Unit = {},
    val onConnection: () -> Unit = {},
    val onSync: () -> Unit = {},
    val onClearLocalData: () -> Unit = {},
    val onHelp: () -> Unit = {},
    val onTerms: () -> Unit = {},
)

private const val CLOCK_TICK_MS = 30_000L

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel,
    navigation: SettingsNavigation,
    onLocalDataCleared: () -> Unit,
    onSessionExpired: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val toast = LocalToast.current
    val now by produceState(Clock.System.now()) {
        while (true) {
            delay(CLOCK_TICK_MS)
            value = Clock.System.now()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.ShowToast -> toast.show(event.message, event.kind)
                SettingsEvent.LocalDataCleared -> onLocalDataCleared()
                SettingsEvent.SessionExpired -> onSessionExpired()
            }
        }
    }

    LifecycleResumeEffect(viewModel) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    SettingsScreen(
        state = state,
        now = now,
        actions = SettingsActions(
            navigation = navigation,
            onLanguage = viewModel::onLanguage,
            onConnection = viewModel::onConnection,
            onSync = viewModel::onSync,
            onClearLocalData = viewModel::onClearLocalData,
            onHelp = viewModel::onHelp,
            onTerms = viewModel::onTerms,
        ),
    )
}

@Composable
fun SettingsScreen(state: SettingsUiState, now: Instant, actions: SettingsActions) {
    val colors = Rango.colors
    val navigation = actions.navigation
    Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
        RangoTopBar(title = stringResource(Res.string.settings_title), onBack = navigation.onBack)
        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 22.dp),
        ) {
            RangoSettingsGroup(stringResource(Res.string.settings_group_preferences)) {
                RangoSettingsRow(
                    icon = RangoIcons.DarkMode,
                    label = stringResource(Res.string.settings_theme),
                    value = state.themeLabel,
                    onClick = navigation.onTheme,
                )
                RangoSettingsRow(
                    icon = RangoIcons.Notifications,
                    label = stringResource(Res.string.settings_notifications),
                    onClick = navigation.onNotifications,
                    showDivider = true,
                )
                RangoSettingsRow(
                    icon = RangoIcons.Language,
                    label = stringResource(Res.string.settings_language),
                    value = stringResource(Res.string.settings_language_value),
                    onClick = actions.onLanguage,
                    showDivider = true,
                )
            }
            RangoSettingsGroup(stringResource(Res.string.settings_group_account)) {
                RangoSettingsRow(
                    icon = RangoIcons.Person,
                    label = stringResource(Res.string.settings_personal_data),
                    onClick = navigation.onPersonalData,
                )
                RangoSettingsRow(
                    icon = RangoIcons.LocationOn,
                    label = stringResource(Res.string.settings_addresses),
                    onClick = navigation.onAddresses,
                    showDivider = true,
                )
                RangoSettingsRow(
                    icon = RangoIcons.Wallet,
                    label = stringResource(Res.string.settings_payments),
                    onClick = navigation.onPayments,
                    showDivider = true,
                )
            }
            RangoSettingsGroup(stringResource(Res.string.settings_group_data)) {
                RangoSettingsRow(
                    icon = RangoIcons.WifiOff,
                    label = stringResource(Res.string.settings_offline),
                    hint = state.connectionHint,
                    switchChecked = state.online == false,
                    showChevron = false,
                    onClick = actions.onConnection,
                )
                RangoSettingsRow(
                    icon = RangoIcons.Sync,
                    label = stringResource(Res.string.settings_sync),
                    hint = if (state.syncing) stringResource(Res.string.settings_syncing) else state.syncHint(now),
                    onClick = actions.onSync,
                    enabled = !state.syncing,
                    showDivider = true,
                )
                RangoSettingsRow(
                    icon = RangoIcons.DeleteSweep,
                    label = stringResource(Res.string.settings_clear),
                    hint = stringResource(Res.string.settings_clear_hint),
                    onClick = actions.onClearLocalData,
                    enabled = !state.clearing,
                    danger = true,
                    showDivider = true,
                )
            }
            RangoSettingsGroup(stringResource(Res.string.settings_group_about)) {
                RangoSettingsRow(
                    icon = RangoIcons.Help,
                    label = stringResource(Res.string.settings_help),
                    onClick = actions.onHelp,
                )
                RangoSettingsRow(
                    icon = RangoIcons.Description,
                    label = stringResource(Res.string.settings_terms),
                    onClick = actions.onTerms,
                    showDivider = true,
                )
            }
            Text(
                text = stringResource(Res.string.settings_version),
                style = Rango.typography.mono.copy(fontSize = 10.sp),
                color = colors.text2,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private val PreviewNow = Instant.fromEpochMilliseconds(1_762_700_000_000)

private val PreviewState = SettingsUiState(
    theme = "light",
    online = true,
    lastSyncedAt = PreviewNow - 8.minutes,
)

@Preview
@Composable
private fun SettingsScreenPreview() {
    RangoTheme(ThemeMode.Light) {
        SettingsScreen(PreviewState, PreviewNow, SettingsActions())
    }
}

@Preview
@Composable
private fun SettingsScreenDarkPreview() {
    RangoTheme(ThemeMode.Dark) {
        SettingsScreen(PreviewState.copy(theme = "dark", online = false), PreviewNow, SettingsActions())
    }
}
