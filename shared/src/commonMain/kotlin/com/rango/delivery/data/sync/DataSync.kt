package com.rango.delivery.data.sync

import com.rango.delivery.data.cart.CartRepository
import com.rango.delivery.data.favorite.FavoriteRepository
import com.rango.delivery.data.profile.ProfileRepository
import com.rango.delivery.data.profile.SettingsDto
import com.rango.delivery.data.theme.ThemeRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Clock
import kotlin.time.Instant

class DataSync(
    private val profileRepository: ProfileRepository,
    private val favoriteRepository: FavoriteRepository,
    private val cartRepository: CartRepository,
    private val themeRepository: ThemeRepository,
    private val clock: () -> Instant = { Clock.System.now() },
) {
    private val _lastSyncedAt = MutableStateFlow<Instant?>(null)
    val lastSyncedAt: StateFlow<Instant?> = _lastSyncedAt.asStateFlow()

    suspend fun syncNow(): SettingsDto = coroutineScope {
        themeRepository.pushPending()
        val settings = async { profileRepository.settings() }
        val favorites = async { favoriteRepository.list() }
        val cart = async { cartRepository.cart() }
        favorites.await()
        cart.await()
        settings.await().also {
            themeRepository.onRemote(it.theme)
            _lastSyncedAt.value = clock()
        }
    }

    fun reset() {
        _lastSyncedAt.value = null
    }
}
