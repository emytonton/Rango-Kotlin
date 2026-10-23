package com.rango.delivery.data.auth

import com.rango.delivery.core.network.ApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AuthorizedCallerTest {

    @Test
    fun expiredTokenIsRenewedAndRequestRetried() = runTest {
        val session = FakeSessionRepository(token = "antigo", renewed = "novo")
        val usedTokens = mutableListOf<String>()

        val result = AuthorizedCaller(session).call { token ->
            usedTokens += token
            if (token == "antigo") throw ApiException(401, "TOKEN_EXPIRED", "Sessão expirada. Renove o token.")
            "ok"
        }

        assertEquals("ok", result)
        assertEquals(listOf("antigo", "novo"), usedTokens)
    }

    @Test
    fun invalidTokenClearsSession() = runTest {
        val session = FakeSessionRepository(token = "qualquer", renewed = null)

        assertFailsWith<SessionExpiredException> {
            AuthorizedCaller(session).call { throw ApiException(401, "INVALID_TOKEN", "Token inválido.") }
        }
        assertTrue(session.cleared)
    }

    @Test
    fun missingSessionFailsWithoutCallingBackend() = runTest {
        var called = false

        assertFailsWith<SessionExpiredException> {
            AuthorizedCaller(FakeSessionRepository(token = null, renewed = null)).call { called = true }
        }
        assertEquals(false, called)
    }

    private class FakeSessionRepository(token: String?, private val renewed: String?) : SessionRepository {
        private val state = MutableStateFlow(token)
        var cleared = false

        override val accessToken: StateFlow<String?> = state
        override suspend fun restore(): SessionRestore = SessionRestore.None
        override suspend fun renewAccessToken(expiredToken: String?): String? = renewed?.also { state.value = it }
        override fun save(session: AuthSessionDto) = Unit
        override fun clear() {
            cleared = true
            state.value = null
        }
    }
}
