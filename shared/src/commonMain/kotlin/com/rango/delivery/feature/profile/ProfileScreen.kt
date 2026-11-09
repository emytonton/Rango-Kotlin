package com.rango.delivery.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rango.delivery.core.designsystem.component.LocalToast
import com.rango.delivery.core.designsystem.component.RangoAvatar
import com.rango.delivery.core.designsystem.component.RangoBadgeIconButton
import com.rango.delivery.core.designsystem.component.RangoBottomBar
import com.rango.delivery.core.designsystem.component.RangoErrorState
import com.rango.delivery.core.designsystem.component.RangoMenuRow
import com.rango.delivery.core.designsystem.component.RangoOutlineButton
import com.rango.delivery.core.designsystem.component.RangoSkeleton
import com.rango.delivery.core.designsystem.component.RangoTab
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.profile_addresses
import com.rango.delivery.resources.profile_edit
import com.rango.delivery.resources.profile_edit_hint
import com.rango.delivery.resources.profile_help
import com.rango.delivery.resources.profile_logout
import com.rango.delivery.resources.profile_logout_done
import com.rango.delivery.resources.profile_notifications
import com.rango.delivery.resources.profile_payments
import com.rango.delivery.resources.profile_retry
import com.rango.delivery.resources.profile_settings
import com.rango.delivery.resources.profile_theme
import com.rango.delivery.resources.profile_title
import org.jetbrains.compose.resources.stringResource

data class ProfileNavigation(
    val onNotifications: () -> Unit = {},
    val onEditProfile: () -> Unit = {},
    val onAddresses: () -> Unit = {},
    val onPayments: () -> Unit = {},
    val onSettings: () -> Unit = {},
    val onTheme: () -> Unit = {},
    val onTab: (RangoTab) -> Unit = {},
)

data class ProfileActions(
    val navigation: ProfileNavigation = ProfileNavigation(),
    val onHelp: () -> Unit = {},
    val onLogout: () -> Unit = {},
    val onRetry: () -> Unit = {},
)

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel,
    navigation: ProfileNavigation,
    onLoggedOut: () -> Unit,
    onSessionExpired: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val toast = LocalToast.current
    val loggedOutMessage = stringResource(Res.string.profile_logout_done)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.ShowToast -> toast.show(event.message, event.kind)
                ProfileEvent.LoggedOut -> {
                    toast.show(loggedOutMessage, ToastKind.Warning)
                    onLoggedOut()
                }
                ProfileEvent.SessionExpired -> onSessionExpired()
            }
        }
    }

    LifecycleResumeEffect(viewModel) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    ProfileScreen(
        state = state,
        actions = ProfileActions(
            navigation = navigation,
            onHelp = viewModel::onHelp,
            onLogout = viewModel::onLogout,
            onRetry = viewModel::retry,
        ),
    )
}

@Composable
fun ProfileScreen(state: ProfileUiState, actions: ProfileActions) {
    val colors = Rango.colors
    Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
        Box(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars).background(colors.surface))
        Column(modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
            ProfileHeader(state, actions.navigation)
            when {
                state.errorMessage != null -> Box(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                    RangoErrorState(
                        message = state.errorMessage,
                        actionText = stringResource(Res.string.profile_retry),
                        onAction = actions.onRetry,
                    )
                }
                else -> ProfileMenu(state, actions)
            }
        }
        RangoBottomBar(selected = RangoTab.Profile, onSelect = actions.navigation.onTab)
    }
}

@Composable
private fun ProfileHeader(state: ProfileUiState, navigation: ProfileNavigation) {
    val colors = Rango.colors
    Column(modifier = Modifier.fillMaxWidth().background(colors.surface)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp),
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 22.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(Res.string.profile_title),
                    style = Rango.typography.title.copy(fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.6).sp),
                    color = colors.text,
                    modifier = Modifier.weight(1f),
                )
                RangoBadgeIconButton(
                    icon = RangoIcons.Notifications,
                    contentDescription = stringResource(Res.string.profile_notifications),
                    onClick = navigation.onNotifications,
                    showDot = state.unreadCount > 0,
                    size = 40.dp,
                )
            }
            if (state.loaded) {
                AccountInfo(state)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    RangoSkeleton(Modifier.size(66.dp), cornerRadius = 22.dp)
                    RangoSkeleton(Modifier.weight(1f).height(58.dp), cornerRadius = 12.dp)
                }
            }
            RangoOutlineButton(text = stringResource(Res.string.profile_edit), onClick = navigation.onEditProfile)
        }
        HorizontalDivider(thickness = 1.dp, color = colors.border)
    }
}

@Composable
private fun AccountInfo(state: ProfileUiState) {
    val colors = Rango.colors
    val detail = Rango.typography.caption.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        RangoAvatar(initials = state.initials, imageUrl = state.avatarUrl)
        Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = state.name,
                style = Rango.typography.title.copy(fontSize = 17.5.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.4).sp),
                color = colors.text,
            )
            Text(text = state.email, style = detail, color = colors.text2)
            if (state.phone.isNotEmpty()) Text(text = state.phone, style = detail, color = colors.text2)
        }
    }
}

@Composable
private fun ProfileMenu(state: ProfileUiState, actions: ProfileActions) {
    val navigation = actions.navigation
    val hint: (String) -> String? = { if (state.loaded) it else null }
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 20.dp),
    ) {
        RangoMenuRow(
            icon = RangoIcons.Person,
            label = stringResource(Res.string.profile_edit),
            hint = stringResource(Res.string.profile_edit_hint),
            onClick = navigation.onEditProfile,
        )
        RangoMenuRow(RangoIcons.LocationOn, stringResource(Res.string.profile_addresses), navigation.onAddresses, hint = hint(state.addressHint))
        RangoMenuRow(RangoIcons.Wallet, stringResource(Res.string.profile_payments), navigation.onPayments, hint = hint(state.paymentHint))
        RangoMenuRow(RangoIcons.Settings, stringResource(Res.string.profile_settings), navigation.onSettings)
        RangoMenuRow(
            RangoIcons.Notifications,
            stringResource(Res.string.profile_notifications),
            navigation.onNotifications,
            hint = hint(state.notificationsHint),
        )
        RangoMenuRow(RangoIcons.DarkMode, stringResource(Res.string.profile_theme), navigation.onTheme, hint = hint(state.themeLabel))
        RangoMenuRow(RangoIcons.Help, stringResource(Res.string.profile_help), actions.onHelp)
        RangoMenuRow(RangoIcons.Logout, stringResource(Res.string.profile_logout), actions.onLogout, danger = true)
    }
}

private val PreviewState = ProfileUiState(
    loading = false,
    loaded = true,
    name = "Marina Alves",
    email = "marina.alves@email.com",
    phone = "(11) 98876-4410",
    addressCount = 3,
    paymentCount = 4,
    unreadCount = 2,
    theme = "light",
)

@Preview
@Composable
private fun ProfileScreenPreview() {
    RangoTheme(ThemeMode.Light) {
        ProfileScreen(PreviewState, ProfileActions())
    }
}

@Preview
@Composable
private fun ProfileScreenDarkPreview() {
    RangoTheme(ThemeMode.Dark) {
        ProfileScreen(PreviewState.copy(unreadCount = 0, theme = "dark"), ProfileActions())
    }
}
