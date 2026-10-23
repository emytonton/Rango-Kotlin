package com.rango.delivery.di

import com.rango.delivery.core.network.createHttpClient
import com.rango.delivery.data.auth.AuthApi
import com.rango.delivery.data.auth.DefaultSessionRepository
import com.rango.delivery.data.auth.SessionRepository
import com.rango.delivery.data.storage.SecureStorage

class AppContainer(secureStorage: SecureStorage) {
    private val httpClient = createHttpClient()
    private val authApi = AuthApi(httpClient)

    val sessionRepository: SessionRepository = DefaultSessionRepository(authApi, secureStorage)
}
