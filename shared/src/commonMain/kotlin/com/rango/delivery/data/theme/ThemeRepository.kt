package com.rango.delivery.data.theme

import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.data.profile.ProfileRepository
import com.rango.delivery.data.storage.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeKeys {
    const val LIGHT = "light"
    const val DARK = "dark"
    const val SYSTEM = "system"

    val all = listOf(LIGHT, DARK, SYSTEM)
}

class ThemeRepository(
    private val storage: SecureStorage,
    private val profileRepository: ProfileRepository,
) {
    private val _theme = MutableStateFlow(storage.get(KEY_THEME)?.takeIf { it in ThemeKeys.all } ?: ThemeKeys.SYSTEM)
    val theme: StateFlow<String> = _theme.asStateFlow()

    val hasPendingSync: Boolean get() = storage.get(KEY_PENDING) != null

    suspend fun select(theme: String): Boolean {
        require(theme in ThemeKeys.all)
        storage.put(KEY_THEME, theme)
        storage.put(KEY_PENDING, theme)
        _theme.value = theme
        return pushPending()
    }

    suspend fun pushPending(): Boolean {
        val pending = storage.get(KEY_PENDING) ?: return true
        return try {
            profileRepository.updateTheme(pending)
            if (storage.get(KEY_PENDING) == pending) storage.remove(KEY_PENDING)
            true
        } catch (e: ApiException) {
            false
        } catch (e: NetworkException) {
            false
        }
    }

    fun onRemote(theme: String) {
        if (hasPendingSync || theme !in ThemeKeys.all) return
        storage.put(KEY_THEME, theme)
        _theme.value = theme
    }

    private companion object {
        const val KEY_THEME = "theme"
        const val KEY_PENDING = "theme_pending"
    }
}
