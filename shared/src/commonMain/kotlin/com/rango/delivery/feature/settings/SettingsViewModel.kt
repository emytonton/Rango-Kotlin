package com.rango.delivery.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.data.auth.SessionExpiredException
import com.rango.delivery.data.auth.SessionRepository
import com.rango.delivery.data.connection.ConnectionRepository
import com.rango.delivery.data.favorite.FavoriteRepository
import com.rango.delivery.data.payment.PaymentSelection
import com.rango.delivery.data.profile.ProfileRepository
import com.rango.delivery.data.sync.DataSync
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Instant

data class SettingsUiState(
    val theme: String = "system",
    val online: Boolean? = null,
    val lastSyncedAt: Instant? = null,
    val syncing: Boolean = false,
    val clearing: Boolean = false,
) {
    val themeLabel: String
        get() = when (theme) {
            "light" -> "Claro"
            "dark" -> "Escuro"
            else -> "Sistema"
        }

    val connectionHint: String
        get() = when (online) {
            true -> "conectado"
            false -> "sem conexão com o servidor"
            null -> "verificando conexão"
        }

    fun syncHint(now: Instant): String = formatLastSync(lastSyncedAt, now)
}

sealed interface SettingsEvent {
    data class ShowToast(val message: String, val kind: ToastKind) : SettingsEvent
    data object LocalDataCleared : SettingsEvent
    data object SessionExpired : SettingsEvent
}

class SettingsViewModel(
    private val profileRepository: ProfileRepository,
    private val connectionRepository: ConnectionRepository,
    private val dataSync: DataSync,
    private val sessionRepository: SessionRepository,
    private val favoriteRepository: FavoriteRepository,
    private val paymentSelection: PaymentSelection,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(lastSyncedAt = dataSync.lastSyncedAt.value))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events: Flow<SettingsEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            dataSync.lastSyncedAt.collect { at -> _uiState.update { it.copy(lastSyncedAt = at) } }
        }
        refresh()
    }

    fun refresh() {
        if (_uiState.value.clearing) return
        viewModelScope.launch { checkConnection() }
        viewModelScope.launch {
            try {
                val settings = profileRepository.settings()
                _uiState.update { it.copy(theme = settings.theme) }
            } catch (e: SessionExpiredException) {
                _events.send(SettingsEvent.SessionExpired)
            } catch (e: ApiException) {
            } catch (e: NetworkException) {
            }
        }
    }

    fun onConnection() {
        viewModelScope.launch {
            val wasOffline = _uiState.value.online == false
            val online = checkConnection()
            when {
                online && wasOffline -> toast("Conexão restaurada.", ToastKind.Success)
                online -> toast("Conectado ao servidor.", ToastKind.Success)
                else -> toast("Sem conexão com o servidor.", ToastKind.Warning)
            }
        }
    }

    fun onSync() {
        if (_uiState.value.syncing) return
        _uiState.update { it.copy(syncing = true) }
        viewModelScope.launch {
            try {
                val settings = dataSync.syncNow()
                _uiState.update { it.copy(theme = settings.theme, online = true) }
                toast("Dados sincronizados.", ToastKind.Success)
            } catch (e: SessionExpiredException) {
                _events.send(SettingsEvent.SessionExpired)
            } catch (e: ApiException) {
                toast(e.message, ToastKind.Error)
            } catch (e: NetworkException) {
                _uiState.update { it.copy(online = false) }
                toast(e.message.orEmpty(), ToastKind.Error)
            } finally {
                _uiState.update { it.copy(syncing = false) }
            }
        }
    }

    fun onClearLocalData() {
        if (_uiState.value.clearing) return
        _uiState.update { it.copy(clearing = true) }
        viewModelScope.launch {
            sessionRepository.logout()
            favoriteRepository.clearLocal()
            paymentSelection.clear()
            dataSync.reset()
            _events.send(SettingsEvent.ShowToast("Dados locais apagados.", ToastKind.Warning))
            _events.send(SettingsEvent.LocalDataCleared)
        }
    }

    fun onLanguage() = toast("Apenas português neste protótipo.", ToastKind.Warning)

    fun onHelp() = toast("Central de ajuda em breve.", ToastKind.Warning)

    fun onTerms() = toast("Documentos abertos no navegador.", ToastKind.Warning)

    private suspend fun checkConnection(): Boolean {
        val online = connectionRepository.isOnline()
        _uiState.update { it.copy(online = online) }
        return online
    }

    private fun toast(message: String, kind: ToastKind) {
        viewModelScope.launch { _events.send(SettingsEvent.ShowToast(message, kind)) }
    }
}

internal fun formatLastSync(lastSyncedAt: Instant?, now: Instant): String {
    if (lastSyncedAt == null) return "ainda não sincronizado nesta sessão"
    val elapsed: Duration = now - lastSyncedAt
    val minutes = elapsed.inWholeMinutes
    return when {
        minutes < 1 -> "última sincronização: agora"
        minutes < 60 -> "última sincronização: há $minutes min"
        else -> "última sincronização: há ${elapsed.inWholeHours} h"
    }
}
