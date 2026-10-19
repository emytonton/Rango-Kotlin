package com.rango.delivery.feature.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rango.delivery.core.designsystem.component.LocalToast
import com.rango.delivery.core.designsystem.component.RangoBackButton
import com.rango.delivery.core.designsystem.component.RangoCheckbox
import com.rango.delivery.core.designsystem.component.RangoPrimaryButton
import com.rango.delivery.core.designsystem.component.RangoTextAction
import com.rango.delivery.core.designsystem.component.RangoTextField
import com.rango.delivery.core.designsystem.icon.RangoIcons
import com.rango.delivery.core.designsystem.theme.Rango
import com.rango.delivery.core.designsystem.theme.RangoTheme
import com.rango.delivery.core.designsystem.theme.ThemeMode
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.signup_confirmation_label
import com.rango.delivery.resources.signup_confirmation_placeholder
import com.rango.delivery.resources.signup_email_label
import com.rango.delivery.resources.signup_email_placeholder
import com.rango.delivery.resources.signup_have_account
import com.rango.delivery.resources.signup_login
import com.rango.delivery.resources.signup_name_label
import com.rango.delivery.resources.signup_name_placeholder
import com.rango.delivery.resources.signup_password_label
import com.rango.delivery.resources.signup_password_placeholder
import com.rango.delivery.resources.signup_phone_label
import com.rango.delivery.resources.signup_phone_placeholder
import com.rango.delivery.resources.signup_privacy_policy
import com.rango.delivery.resources.signup_submit
import com.rango.delivery.resources.signup_submitting
import com.rango.delivery.resources.signup_terms
import com.rango.delivery.resources.signup_terms_of_use
import com.rango.delivery.resources.signup_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private data class FieldSpec(
    val field: SignupField,
    val label: StringResource,
    val placeholder: StringResource,
    val icon: ImageVector,
    val keyboardType: KeyboardType,
    val password: Boolean = false,
)

private val fieldSpecs = listOf(
    FieldSpec(SignupField.Name, Res.string.signup_name_label, Res.string.signup_name_placeholder, RangoIcons.Person, KeyboardType.Text),
    FieldSpec(SignupField.Email, Res.string.signup_email_label, Res.string.signup_email_placeholder, RangoIcons.Mail, KeyboardType.Email),
    FieldSpec(SignupField.Phone, Res.string.signup_phone_label, Res.string.signup_phone_placeholder, RangoIcons.Call, KeyboardType.Phone),
    FieldSpec(SignupField.Password, Res.string.signup_password_label, Res.string.signup_password_placeholder, RangoIcons.Lock, KeyboardType.Password, password = true),
    FieldSpec(SignupField.Confirmation, Res.string.signup_confirmation_label, Res.string.signup_confirmation_placeholder, RangoIcons.LockReset, KeyboardType.Password, password = true),
)

@Composable
fun SignupRoute(
    viewModel: SignupViewModel,
    onBack: () -> Unit,
    onLogin: () -> Unit,
    onRegistered: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val toast = LocalToast.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SignupEvent.ShowToast -> toast.show(event.message, event.kind)
                SignupEvent.Registered -> onRegistered()
            }
        }
    }

    SignupScreen(
        state = state,
        onFieldChange = viewModel::onFieldChange,
        onTermsToggle = viewModel::onTermsToggle,
        onSubmit = viewModel::onSubmit,
        onBack = onBack,
        onLogin = onLogin,
    )
}

@Composable
fun SignupScreen(
    state: SignupUiState,
    onFieldChange: (SignupField, String) -> Unit,
    onTermsToggle: () -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onLogin: () -> Unit,
) {
    val colors = Rango.colors
    val focusManager = LocalFocusManager.current
    val errors = state.errors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 8.dp),
        ) {
            RangoBackButton(onClick = onBack)
            Text(
                text = stringResource(Res.string.signup_title),
                style = Rango.typography.button,
                color = colors.text,
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 26.dp),
        ) {
            fieldSpecs.forEachIndexed { index, spec ->
                val last = index == fieldSpecs.lastIndex
                RangoTextField(
                    label = stringResource(spec.label),
                    value = state.form.valueOf(spec.field),
                    onValueChange = { onFieldChange(spec.field, it) },
                    leadingIcon = spec.icon,
                    placeholder = stringResource(spec.placeholder),
                    error = errors[spec.field],
                    valid = state.isValid(spec.field),
                    password = spec.password,
                    keyboardType = spec.keyboardType,
                    imeAction = if (last) ImeAction.Done else ImeAction.Next,
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                )
            }
            TermsRow(accepted = state.termsAccepted, onToggle = onTermsToggle)
            RangoPrimaryButton(
                text = stringResource(Res.string.signup_submit),
                onClick = onSubmit,
                loading = state.loading,
                loadingText = stringResource(Res.string.signup_submitting),
                modifier = Modifier
                    .padding(top = 4.dp)
                    .alpha(if (state.termsAccepted) 1f else 0.55f),
            )
            RangoTextAction(
                prompt = stringResource(Res.string.signup_have_account),
                action = stringResource(Res.string.signup_login),
                onClick = onLogin,
            )
        }
    }
}

@Composable
private fun TermsRow(accepted: Boolean, onToggle: () -> Unit) {
    val colors = Rango.colors
    val termsOfUse = stringResource(Res.string.signup_terms_of_use)
    val privacy = stringResource(Res.string.signup_privacy_policy)
    val full = stringResource(Res.string.signup_terms, termsOfUse, privacy)
    val text = remember(full, termsOfUse, privacy, colors.primary) {
        highlight(full, listOf(termsOfUse, privacy), SpanStyle(color = colors.primary, fontWeight = FontWeight.Bold))
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onToggle)
            .padding(vertical = 2.dp),
    ) {
        RangoCheckbox(checked = accepted)
        Text(
            text = text,
            style = Rango.typography.label.copy(fontWeight = FontWeight.Medium, lineHeight = 17.sp),
            color = colors.text2,
        )
    }
}

private fun highlight(text: String, parts: List<String>, style: SpanStyle): AnnotatedString = buildAnnotatedString {
    var cursor = 0
    parts.forEach { part ->
        val start = text.indexOf(part, cursor)
        if (start < 0) return@forEach
        append(text.substring(cursor, start))
        withStyle(style) { append(part) }
        cursor = start + part.length
    }
    append(text.substring(cursor))
}

@Preview
@Composable
private fun SignupScreenPreview() {
    RangoTheme(ThemeMode.Light) {
        SignupScreen(
            state = SignupUiState(),
            onFieldChange = { _, _ -> },
            onTermsToggle = {},
            onSubmit = {},
            onBack = {},
            onLogin = {},
        )
    }
}

@Preview
@Composable
private fun SignupScreenErrorsDarkPreview() {
    RangoTheme(ThemeMode.Dark) {
        SignupScreen(
            state = SignupUiState(
                form = SignupForm(name = "Marina Alves", email = "marina@", phone = "1199"),
                submitted = true,
            ),
            onFieldChange = { _, _ -> },
            onTermsToggle = {},
            onSubmit = {},
            onBack = {},
            onLogin = {},
        )
    }
}
