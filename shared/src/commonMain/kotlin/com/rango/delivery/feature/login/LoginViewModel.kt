package com.rango.delivery.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.core.validation.isValidEmail
import com.rango.delivery.data.auth.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val keepSignedIn: Boolean = true,
    val emailError: String? = null,
    val passwordError: String? = null,
    val loading: Boolean = false,
    val success: Boolean = false,
)

sealed interface LoginEvent {
    data class ShowToast(val message: String, val kind: ToastKind) : LoginEvent
    data object LoggedIn : LoginEvent
}

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val successDelayMillis: Long = SUCCESS_DELAY_MILLIS,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events: Flow<LoginEvent> = _events.receiveAsFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun onToggleKeepSignedIn() {
        _uiState.update { it.copy(keepSignedIn = !it.keepSignedIn) }
    }

    fun onForgotPassword() {
        send(LoginEvent.ShowToast("Recuperação de senha indisponível no momento.", ToastKind.Warning))
    }

    fun onSubmit() {
        val state = _uiState.value
        if (state.loading || state.success) return
        val emailError = when {
            state.email.isBlank() -> "Informe seu e-mail."
            !isValidEmail(state.email) -> "E-mail inválido."
            else -> null
        }
        val passwordError = if (state.password.isEmpty()) "Informe sua senha." else null
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        login(state)
    }

    private fun login(state: LoginUiState) {
        _uiState.update { it.copy(loading = true, emailError = null, passwordError = null) }
        viewModelScope.launch {
            try {
                authRepository.login(state.email, state.password, state.keepSignedIn)
                _uiState.update { it.copy(loading = false, success = true) }
                delay(successDelayMillis)
                _events.send(LoginEvent.LoggedIn)
            } catch (e: ApiException) {
                _uiState.update { it.copy(loading = false) }
                handleApiError(e)
            } catch (e: NetworkException) {
                _uiState.update { it.copy(loading = false) }
                send(LoginEvent.ShowToast(e.message.orEmpty(), ToastKind.Error))
            }
        }
    }

    private fun handleApiError(e: ApiException) {
        if (e.code == "INVALID_CREDENTIALS") {
            _uiState.update { it.copy(passwordError = "Senha incorreta. Tente novamente.") }
            return
        }
        val emailError = e.details.firstOrNull { it.field == "email" }?.message
        val passwordError = e.details.firstOrNull { it.field == "password" }?.message
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
        } else {
            send(LoginEvent.ShowToast(e.message, ToastKind.Error))
        }
    }

    private fun send(event: LoginEvent) {
        viewModelScope.launch { _events.send(event) }
    }

    private companion object {
        const val SUCCESS_DELAY_MILLIS = 700L
    }
}
