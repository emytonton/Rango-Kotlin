package com.rango.delivery.data.auth

import com.rango.delivery.core.network.ApiException

class SessionExpiredException : Exception("Sua sessão expirou. Entre novamente.")

class AuthorizedCaller(private val sessionRepository: SessionRepository) {

    suspend fun <T> call(request: suspend (accessToken: String) -> T): T {
        val token = sessionRepository.accessToken.value
            ?: sessionRepository.renewAccessToken(null)
            ?: throw SessionExpiredException()
        return try {
            request(token)
        } catch (e: ApiException) {
            if (e.status != 401) throw e
            if (e.code != "TOKEN_EXPIRED") {
                sessionRepository.clear()
                throw SessionExpiredException()
            }
            val renewed = sessionRepository.renewAccessToken(token) ?: throw SessionExpiredException()
            request(renewed)
        }
    }
}
