package com.rango.delivery.feature.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.data.auth.AuthRepository
import com.rango.delivery.data.auth.SignupData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignupUiState(
    val form: SignupForm = SignupForm(),
    val termsAccepted: Boolean = false,
    val submitted: Boolean = false,
    val loading: Boolean = false,
    val serverErrors: Map<SignupField, String> = emptyMap(),
) {
    val errors: Map<SignupField, String>
        get() = (if (submitted) SignupValidator.validate(form) else emptyMap()) + serverErrors

    fun isValid(field: SignupField): Boolean =
        submitted && field !in errors && form.valueOf(field).isNotEmpty()
}

sealed interface SignupEvent {
    data class ShowToast(val message: String, val kind: ToastKind) : SignupEvent
    data object Registered : SignupEvent
}

class SignupViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    private val _events = Channel<SignupEvent>(Channel.BUFFERED)
    val events: Flow<SignupEvent> = _events.receiveAsFlow()

    fun onFieldChange(field: SignupField, value: String) {
        _uiState.update { it.copy(form = it.form.with(field, value), serverErrors = it.serverErrors - field) }
    }

    fun onTermsToggle() {
        _uiState.update { it.copy(termsAccepted = !it.termsAccepted) }
    }

    fun onSubmit() {
        val state = _uiState.value
        if (state.loading) return
        _uiState.update { it.copy(submitted = true) }
        val hasErrors = SignupValidator.validate(state.form).isNotEmpty()
        if (hasErrors || !state.termsAccepted) {
            if (!state.termsAccepted) toast("Aceite os termos para continuar.", ToastKind.Error)
            return
        }
        register(state.form)
    }

    private fun register(form: SignupForm) {
        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch {
            try {
                authRepository.register(
                    SignupData(form.name, form.email, form.phone, form.password, form.confirmation),
                )
                _events.send(SignupEvent.ShowToast("Conta criada com sucesso!", ToastKind.Success))
                _events.send(SignupEvent.Registered)
            } catch (e: ApiException) {
                val fieldErrors = e.details.mapNotNull { detail ->
                    SignupField.entries.firstOrNull { it.apiName == detail.field }?.let { it to detail.message }
                }.toMap()
                _uiState.update { it.copy(serverErrors = fieldErrors) }
                if (fieldErrors.isEmpty()) toast(e.message, ToastKind.Error)
            } catch (e: NetworkException) {
                toast(e.message.orEmpty(), ToastKind.Error)
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }

    private fun toast(message: String, kind: ToastKind) {
        viewModelScope.launch { _events.send(SignupEvent.ShowToast(message, kind)) }
    }
}
