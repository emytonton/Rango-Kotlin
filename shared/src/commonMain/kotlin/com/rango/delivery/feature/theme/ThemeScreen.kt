package com.rango.delivery.feature.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rango.delivery.core.designsystem.component.RangoSettingsRow
import com.rango.delivery.core.designsystem.component.RangoThemePreview
import com.rango.delivery.core.designsystem.component.RangoTopBar
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.DarkRangoColors
import com.rango.delivery.core.designsystem.theme.LightRangoColors
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode
import com.rango.delivery.data.theme.ThemeKeys
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.theme_dark
import com.rango.delivery.resources.theme_dark_hint
import com.rango.delivery.resources.theme_light
import com.rango.delivery.resources.theme_light_hint
import com.rango.delivery.resources.theme_system
import com.rango.delivery.resources.theme_system_hint
import com.rango.delivery.resources.theme_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private data class ThemeOption(
    val key: String,
    val label: StringResource,
    val hint: StringResource,
    val icon: ImageVector,
)

private val Options = listOf(
    ThemeOption(ThemeKeys.LIGHT, Res.string.theme_light, Res.string.theme_light_hint, RangoIcons.LightMode),
    ThemeOption(ThemeKeys.DARK, Res.string.theme_dark, Res.string.theme_dark_hint, RangoIcons.DarkMode),
    ThemeOption(ThemeKeys.SYSTEM, Res.string.theme_system, Res.string.theme_system_hint, RangoIcons.BrightnessAuto),
)

private val GroupShape = RoundedCornerShape(20.dp)

@Composable
fun ThemeRoute(
    viewModel: ThemeViewModel,
    onBack: () -> Unit,
    onSessionExpired: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ThemeEvent.SessionExpired -> onSessionExpired()
            }
        }
    }

    ThemeScreen(
        state = state,
        systemDark = isSystemInDarkTheme(),
        onSelect = viewModel::onSelect,
        onBack = onBack,
    )
}

@Composable
fun ThemeScreen(
    state: ThemeUiState,
    systemDark: Boolean,
    onSelect: (String) -> Unit,
    onBack: () -> Unit,
) {
    val colors = Rango.colors
    Column(modifier = Modifier.fillMaxSize().background(colors.background)) {
        RangoTopBar(title = stringResource(Res.string.theme_title), onBack = onBack)
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 22.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(11.dp), modifier = Modifier.fillMaxWidth()) {
                RangoThemePreview(
                    name = stringResource(Res.string.theme_light),
                    palette = LightRangoColors,
                    selected = state.isPreviewActive(previewDark = false, systemDark = systemDark),
                    modifier = Modifier.weight(1f),
                )
                RangoThemePreview(
                    name = stringResource(Res.string.theme_dark),
                    palette = DarkRangoColors,
                    selected = state.isPreviewActive(previewDark = true, systemDark = systemDark),
                    modifier = Modifier.weight(1f),
                )
            }
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
                        radioSelected = state.theme == option.key,
                        showChevron = false,
                        showDivider = index > 0,
                        onClick = { onSelect(option.key) },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ThemeScreenPreview() {
    RangoTheme(ThemeMode.Light) {
        ThemeScreen(ThemeUiState(ThemeKeys.LIGHT), systemDark = false, onSelect = {}, onBack = {})
    }
}

@Preview
@Composable
private fun ThemeScreenDarkPreview() {
    RangoTheme(ThemeMode.Dark) {
        ThemeScreen(ThemeUiState(ThemeKeys.SYSTEM), systemDark = true, onSelect = {}, onBack = {})
    }
}
