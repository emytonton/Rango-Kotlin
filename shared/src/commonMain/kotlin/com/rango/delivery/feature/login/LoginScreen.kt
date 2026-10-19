package com.rango.delivery.feature.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rango.delivery.core.designsystem.component.LocalToast
import com.rango.delivery.core.designsystem.component.RangoBackButton
import com.rango.delivery.core.designsystem.component.RangoCheckboxLabel
import com.rango.delivery.core.designsystem.component.RangoPasswordToggle
import com.rango.delivery.core.designsystem.component.RangoPrimaryButton
import com.rango.delivery.core.designsystem.component.RangoTextAction
import com.rango.delivery.core.designsystem.component.RangoTextField
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.login_create_account
import com.rango.delivery.resources.login_email_label
import com.rango.delivery.resources.login_email_placeholder
import com.rango.delivery.resources.login_forgot_password
import com.rango.delivery.resources.login_keep_signed_in
import com.rango.delivery.resources.login_no_account
import com.rango.delivery.resources.login_password_label
import com.rango.delivery.resources.login_password_placeholder
import com.rango.delivery.resources.login_submit
import com.rango.delivery.resources.login_submitting
import com.rango.delivery.resources.login_subtitle
import com.rango.delivery.resources.login_success
import com.rango.delivery.resources.login_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginRoute(
    viewModel: LoginViewModel,
    onBack: () -> Unit,
    onSignup: () -> Unit,
    onLoggedIn: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val toast = LocalToast.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.ShowToast -> toast.show(event.message, event.kind)
                LoginEvent.LoggedIn -> onLoggedIn()
            }
        }
    }

    LoginScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onToggleKeepSignedIn = viewModel::onToggleKeepSignedIn,
        onForgotPassword = viewModel::onForgotPassword,
        onSubmit = viewModel::onSubmit,
        onBack = onBack,
        onSignup = onSignup,
    )
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleKeepSignedIn: () -> Unit,
    onForgotPassword: () -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onSignup: () -> Unit,
) {
    val colors = Rango.colors
    val typography = Rango.typography
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        RangoBackButton(onClick = onBack, modifier = Modifier.padding(start = 22.dp, top = 8.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 24.dp),
        ) {
            Text(
                text = stringResource(Res.string.login_title),
                style = typography.title.copy(fontSize = 27.sp, letterSpacing = (-0.9).sp),
                color = colors.text,
            )
            Text(
                text = stringResource(Res.string.login_subtitle),
                style = typography.body,
                color = colors.text2,
                modifier = Modifier.padding(top = 7.dp),
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(15.dp),
                modifier = Modifier.padding(top = 24.dp),
            ) {
                RangoTextField(
                    label = stringResource(Res.string.login_email_label),
                    value = state.email,
                    onValueChange = onEmailChange,
                    leadingIcon = RangoIcons.Mail,
                    placeholder = stringResource(Res.string.login_email_placeholder),
                    error = state.emailError,
                    keyboardType = KeyboardType.Email,
                )
                RangoTextField(
                    label = stringResource(Res.string.login_password_label),
                    value = state.password,
                    onValueChange = onPasswordChange,
                    leadingIcon = RangoIcons.Lock,
                    placeholder = stringResource(Res.string.login_password_placeholder),
                    error = state.passwordError,
                    password = !state.passwordVisible,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        onSubmit()
                    }),
                    trailing = { RangoPasswordToggle(visible = state.passwordVisible, onToggle = onTogglePasswordVisibility) },
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    RangoCheckboxLabel(
                        checked = state.keepSignedIn,
                        label = stringResource(Res.string.login_keep_signed_in),
                        onToggle = onToggleKeepSignedIn,
                    )
                    Text(
                        text = stringResource(Res.string.login_forgot_password),
                        style = typography.label.copy(fontSize = 12.5.sp, fontWeight = FontWeight.Bold),
                        color = colors.primary,
                        modifier = Modifier.clickable(onClick = onForgotPassword),
                    )
                }
                RangoPrimaryButton(
                    text = stringResource(Res.string.login_submit),
                    onClick = {
                        focusManager.clearFocus()
                        onSubmit()
                    },
                    loading = state.loading,
                    loadingText = stringResource(Res.string.login_submitting),
                    successText = if (state.success) stringResource(Res.string.login_success) else null,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        RangoTextAction(
            prompt = stringResource(Res.string.login_no_account),
            action = stringResource(Res.string.login_create_account),
            onClick = onSignup,
            modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 24.dp),
        )
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    RangoTheme(ThemeMode.Light) {
        LoginScreen(
            state = LoginUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onToggleKeepSignedIn = {},
            onForgotPassword = {},
            onSubmit = {},
            onBack = {},
            onSignup = {},
        )
    }
}

@Preview
@Composable
private fun LoginScreenErrorDarkPreview() {
    RangoTheme(ThemeMode.Dark) {
        LoginScreen(
            state = LoginUiState(
                email = "marina.alves@email.com",
                password = "rango12",
                passwordError = "Senha incorreta. Tente novamente.",
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onToggleKeepSignedIn = {},
            onForgotPassword = {},
            onSubmit = {},
            onBack = {},
            onSignup = {},
        )
    }
}
