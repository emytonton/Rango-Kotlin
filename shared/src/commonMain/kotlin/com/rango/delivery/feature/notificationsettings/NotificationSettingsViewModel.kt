package com.rango.delivery.feature.notificationsettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rango.delivery.core.designsystem.component.ToastKind
import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.data.auth.SessionExpiredException
import com.rango.delivery.data.profile.NotificationPrefsDto
import com.rango.delivery.data.profile.ProfileRepository
import com.rango.delivery.data.push.LocalNotifier
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NotificationPref { Orders, Promos, News }

data class NotificationSettingsUiState(
    val loading: Boolean = true,
    val prefs: NotificationPrefsDto? = null,
    val errorMessage: String? = null,
) {
    fun isOn(pref: NotificationPref): Boolean = prefs?.let { it[pref] } ?: false
}

sealed interface NotificationSettingsEvent {
    data class ShowToast(val message: String, val kind: ToastKind) : NotificationSettingsEvent
    data object SessionExpired : NotificationSettingsEvent
}

class NotificationSettingsViewModel(
    private val profileRepository: ProfileRepository,
    private val notifier: LocalNotifier,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

    private val _events = Channel<NotificationSettingsEvent>(Channel.BUFFERED)
    val events: Flow<NotificationSettingsEvent> = _events.receiveAsFlow()

    private var confirmed: NotificationPrefsDto? = null
    private var saveJob: Job? = null

    init {
        load()
    }

    fun retry() {
        load()
    }

    fun onToggle(pref: NotificationPref) {
        val current = _uiState.value.prefs ?: return
        val next = current.with(pref, !current[pref])
        _uiState.update { it.copy(prefs = next) }
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            try {
                val saved = profileRepository.updateNotifications(next).notifications
                confirmed = saved
                _uiState.update { it.copy(prefs = saved) }
            } catch (e: SessionExpiredException) {
                _events.send(NotificationSettingsEvent.SessionExpired)
            } catch (e: ApiException) {
                revert()
            } catch (e: NetworkException) {
                revert()
            }
        }
    }

    fun sendTest() {
        notifier.show(TEST_NOTIFICATION_ID, TEST_TITLE, TEST_BODY)
    }

    private suspend fun revert() {
        _uiState.update { it.copy(prefs = confirmed) }
        _events.send(NotificationSettingsEvent.ShowToast("Não foi possível salvar a preferência.", ToastKind.Error))
    }

    private fun load() {
        _uiState.update { it.copy(loading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val prefs = profileRepository.settings().notifications
                confirmed = prefs
                _uiState.update { it.copy(loading = false, prefs = prefs) }
            } catch (e: SessionExpiredException) {
                _events.send(NotificationSettingsEvent.SessionExpired)
            } catch (e: ApiException) {
                fail(e.message)
            } catch (e: NetworkException) {
                fail("Não foi possível carregar as preferências.")
            }
        }
    }

    private fun fail(message: String?) {
        _uiState.update {
            it.copy(loading = false, errorMessage = message ?: "Não foi possível carregar as preferências.")
        }
    }

    private companion object {
        const val TEST_NOTIFICATION_ID = 900_001L
        const val TEST_TITLE = "Notificação de teste"
        const val TEST_BODY = "Tudo certo: as notificações do Rango estão chegando."
    }
}

private operator fun NotificationPrefsDto.get(pref: NotificationPref): Boolean = when (pref) {
    NotificationPref.Orders -> orders
    NotificationPref.Promos -> promos
    NotificationPref.News -> news
}

private fun NotificationPrefsDto.with(pref: NotificationPref, value: Boolean): NotificationPrefsDto = when (pref) {
    NotificationPref.Orders -> copy(orders = value)
    NotificationPref.Promos -> copy(promos = value)
    NotificationPref.News -> copy(news = value)
}
