package com.rango.delivery.feature.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rango.delivery.data.auth.SessionRepository
import com.rango.delivery.data.auth.SessionRestore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface WelcomeUiState {
    data object CheckingSession : WelcomeUiState
    data class Ready(val savedSessionName: String? = null) : WelcomeUiState
}

sealed interface WelcomeEvent {
    data object SessionRestored : WelcomeEvent
}

class WelcomeViewModel(
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WelcomeUiState>(WelcomeUiState.CheckingSession)
    val uiState: StateFlow<WelcomeUiState> = _uiState.asStateFlow()

    private val _events = Channel<WelcomeEvent>(Channel.BUFFERED)
    val events: Flow<WelcomeEvent> = _events.receiveAsFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            when (val result = sessionRepository.restore()) {
                SessionRestore.None -> _uiState.value = WelcomeUiState.Ready()
                is SessionRestore.Unreachable -> _uiState.value = WelcomeUiState.Ready(result.userName.ifBlank { null })
                is SessionRestore.Restored -> _events.send(WelcomeEvent.SessionRestored)
            }
        }
    }
}
