package com.rango.delivery.feature.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rango.delivery.data.auth.SessionExpiredException
import com.rango.delivery.data.theme.ThemeKeys
import com.rango.delivery.data.theme.ThemeRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ThemeUiState(val theme: String = ThemeKeys.SYSTEM) {
    fun isPreviewActive(previewDark: Boolean, systemDark: Boolean): Boolean = when (theme) {
        ThemeKeys.LIGHT -> !previewDark
        ThemeKeys.DARK -> previewDark
        else -> previewDark == systemDark
    }
}

sealed interface ThemeEvent {
    data object SessionExpired : ThemeEvent
}

class ThemeViewModel(private val themeRepository: ThemeRepository) : ViewModel() {

    val uiState: StateFlow<ThemeUiState> = themeRepository.theme
        .map { ThemeUiState(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeUiState(themeRepository.theme.value))

    private val _events = Channel<ThemeEvent>(Channel.BUFFERED)
    val events: Flow<ThemeEvent> = _events.receiveAsFlow()

    init {
        sync { themeRepository.pushPending() }
    }

    fun onSelect(theme: String) {
        if (theme == uiState.value.theme) return
        sync { themeRepository.select(theme) }
    }

    private fun sync(block: suspend () -> Boolean) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: SessionExpiredException) {
                _events.send(ThemeEvent.SessionExpired)
            }
        }
    }
}
