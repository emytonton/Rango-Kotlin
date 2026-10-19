package com.rango.delivery.data.auth

import com.rango.delivery.core.network.ApiException
import com.rango.delivery.core.network.NetworkException
import com.rango.delivery.data.storage.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface SessionRestore {
    data object None : SessionRestore
    data class Restored(val userName: String) : SessionRestore
    data class Unreachable(val userName: String) : SessionRestore
}

interface SessionRepository {
    val accessToken: StateFlow<String?>
    suspend fun restore(): SessionRestore
    fun save(session: AuthSessionDto)
    fun clear()
}

class DefaultSessionRepository(
    private val api: AuthApi,
    private val storage: SecureStorage,
) : SessionRepository {

    private val _accessToken = MutableStateFlow<String?>(null)
    override val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val refreshMutex = Mutex()

    override suspend fun restore(): SessionRestore = refreshMutex.withLock {
        val refreshToken = storage.get(KEY_REFRESH) ?: return SessionRestore.None
        val userName = storage.get(KEY_USER_NAME).orEmpty()
        try {
            val session = api.refresh(refreshToken)
            save(session)
            SessionRestore.Restored(session.user.name)
        } catch (e: ApiException) {
            if (e.status == 401) {
                clear()
                SessionRestore.None
            } else {
                SessionRestore.Unreachable(userName)
            }
        } catch (e: NetworkException) {
            SessionRestore.Unreachable(userName)
        }
    }

    override fun save(session: AuthSessionDto) {
        storage.put(KEY_REFRESH, session.refreshToken)
        storage.put(KEY_USER_NAME, session.user.name)
        _accessToken.value = session.accessToken
    }

    override fun clear() {
        storage.remove(KEY_REFRESH)
        storage.remove(KEY_USER_NAME)
        _accessToken.value = null
    }

    private companion object {
        const val KEY_REFRESH = "refresh_token"
        const val KEY_USER_NAME = "user_name"
    }
}
