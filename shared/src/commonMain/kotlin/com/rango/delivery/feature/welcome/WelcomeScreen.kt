package com.rango.delivery.feature.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rango.delivery.core.designsystem.component.RangoBrandMark
import com.rango.delivery.core.designsystem.component.RangoInfoBanner
import com.rango.delivery.core.designsystem.component.RangoPrimaryButton
import com.rango.delivery.core.designsystem.component.RangoSecondaryButton
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoRadius
import com.rango.delivery.core.designsystem.theme.RangoSpacing
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.welcome_hero
import com.rango.delivery.resources.welcome_login
import com.rango.delivery.resources.welcome_saved_session
import com.rango.delivery.resources.welcome_signup
import com.rango.delivery.resources.welcome_subtitle
import com.rango.delivery.resources.welcome_terms
import com.rango.delivery.resources.welcome_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun WelcomeRoute(
    viewModel: WelcomeViewModel,
    onLogin: () -> Unit,
    onSignup: () -> Unit,
    onSessionRestored: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                WelcomeEvent.SessionRestored -> onSessionRestored()
            }
        }
    }

    when (val current = state) {
        WelcomeUiState.CheckingSession -> WelcomeSplash()
        is WelcomeUiState.Ready -> WelcomeScreen(
            savedSessionName = current.savedSessionName,
            onLogin = onLogin,
            onSignup = onSignup,
        )
    }
}

@Composable
fun WelcomeScreen(
    savedSessionName: String?,
    onLogin: () -> Unit,
    onSignup: () -> Unit,
) {
    val colors = Rango.colors
    val typography = Rango.typography
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = RangoSpacing.screenHorizontal, end = RangoSpacing.screenHorizontal, top = 14.dp),
        ) {
            RangoBrandMark()
            Image(
                painter = painterResource(Res.drawable.welcome_hero),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(top = 22.dp)
                    .fillMaxWidth()
                    .height(318.dp)
                    .clip(RoundedCornerShape(RangoRadius.xl)),
            )
            Text(
                text = stringResource(Res.string.welcome_title),
                style = typography.display,
                color = colors.text,
                modifier = Modifier.padding(top = 26.dp),
            )
            Text(
                text = stringResource(Res.string.welcome_subtitle),
                style = typography.bodyLarge,
                color = colors.text2,
                modifier = Modifier.padding(top = 10.dp),
            )
            if (savedSessionName != null) {
                RangoInfoBanner(
                    icon = RangoIcons.Lock,
                    text = stringResource(Res.string.welcome_saved_session, savedSessionName),
                    modifier = Modifier.padding(top = 18.dp),
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(11.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(start = RangoSpacing.screenHorizontal, end = RangoSpacing.screenHorizontal, top = 18.dp, bottom = 30.dp),
        ) {
            RangoPrimaryButton(text = stringResource(Res.string.welcome_login), onClick = onLogin)
            RangoSecondaryButton(text = stringResource(Res.string.welcome_signup), onClick = onSignup)
            Text(
                text = stringResource(Res.string.welcome_terms),
                style = typography.caption,
                color = colors.text2,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun WelcomeSplash() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Rango.colors.surface),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            RangoBrandMark()
            Spacer(Modifier.height(RangoSpacing.xl))
            CircularProgressIndicator(
                color = Rango.colors.primary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Preview
@Composable
private fun WelcomeScreenPreview() {
    RangoTheme(ThemeMode.Light) {
        WelcomeScreen(savedSessionName = null, onLogin = {}, onSignup = {})
    }
}

@Preview
@Composable
private fun WelcomeScreenSavedSessionDarkPreview() {
    RangoTheme(ThemeMode.Dark) {
        WelcomeScreen(savedSessionName = "Marina Alves", onLogin = {}, onSignup = {})
    }
}
