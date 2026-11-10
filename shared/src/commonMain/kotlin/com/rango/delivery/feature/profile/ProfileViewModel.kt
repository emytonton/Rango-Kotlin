package com.rango.delivery.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.core.network.apiBaseUrl
import com.rango.delivery.data.address.AddressRepository
import com.rango.delivery.data.auth.SessionExpiredException
import com.rango.delivery.data.auth.SessionRepository
import com.rango.delivery.data.payment.PaymentRepository
import com.rango.delivery.data.profile.ProfileRepository
import com.rango.delivery.data.theme.ThemeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val loading: Boolean = true,
    val loaded: Boolean = false,
    val errorMessage: String? = null,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val avatarUrl: String? = null,
    val addressCount: Int = 0,
    val paymentCount: Int = 0,
    val unreadCount: Int = 0,
    val theme: String = "system",
    val loggingOut: Boolean = false,
) {
    val initials: String
        get() = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

    val addressHint: String get() = if (addressCount == 1) "1 salvo" else "$addressCount salvos"

    val paymentHint: String get() = if (paymentCount == 1) "1 método disponível" else "$paymentCount métodos disponíveis"

    val notificationsHint: String get() = if (unreadCount > 0) "$unreadCount não lidas" else "tudo lido"

    val themeLabel: String
        get() = when (theme) {
            "light" -> "Claro"
            "dark" -> "Escuro"
            else -> "Sistema"
        }
}

sealed interface ProfileEvent {
    data class ShowToast(val message: String, val kind: ToastKind) : ProfileEvent
    data object LoggedOut : ProfileEvent
    data object SessionExpired : ProfileEvent
}

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val addressRepository: AddressRepository,
    private val paymentRepository: PaymentRepository,
    private val sessionRepository: SessionRepository,
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
    val events: Flow<ProfileEvent> = _events.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            themeRepository.theme.collect { theme -> _uiState.update { it.copy(theme = theme) } }
        }
        load()
    }

    fun retry() {
        load()
    }

    fun refresh() {
        if (_uiState.value.loaded) load()
    }

    fun onHelp() {
        viewModelScope.launch { _events.send(ProfileEvent.ShowToast("Central de ajuda em breve.", ToastKind.Warning)) }
    }

    fun onLogout() {
        if (_uiState.value.loggingOut) return
        _uiState.update { it.copy(loggingOut = true) }
        loadJob?.cancel()
        viewModelScope.launch {
            sessionRepository.logout()
            _events.send(ProfileEvent.LoggedOut)
        }
    }

    private fun load() {
        loadJob?.cancel()
        _uiState.update { it.copy(loading = true, errorMessage = null) }
        loadJob = viewModelScope.launch {
            try {
                coroutineScope {
                    val user = async { profileRepository.me() }
                    val settings = async { profileRepository.settings() }
                    val unread = async { profileRepository.unreadNotifications() }
                    val addresses = async { addressRepository.list() }
                    val payments = async { paymentRepository.list() }
                    val me = user.await()
                    themeRepository.onRemote(settings.await().theme)
                    _uiState.update {
                        it.copy(
                            loading = false,
                            loaded = true,
                            name = me.name,
                            email = me.email,
                            phone = me.phone,
                            avatarUrl = me.avatarUrl?.let { url -> apiBaseUrl + url },
                            unreadCount = unread.await(),
                            addressCount = addresses.await().size,
                            paymentCount = payments.await().size,
                        )
                    }
                }
            } catch (e: SessionExpiredException) {
                _events.send(ProfileEvent.SessionExpired)
            } catch (e: ApiException) {
                fail(e.message)
            } catch (e: NetworkException) {
                fail("Não foi possível carregar o perfil.")
            }
        }
    }

    private fun fail(message: String) {
        _uiState.update { if (it.loaded) it.copy(loading = false) else it.copy(loading = false, errorMessage = message) }
    }
}
