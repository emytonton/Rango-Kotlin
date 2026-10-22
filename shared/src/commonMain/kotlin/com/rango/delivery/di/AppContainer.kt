package com.rango.delivery.di

import com.rango.delivery.core.network.createHttpClient
import com.rango.delivery.data.auth.AuthApi
import com.rango.delivery.data.auth.AuthRepository
import com.rango.delivery.data.auth.AuthorizedCaller
import com.rango.delivery.data.auth.DefaultAuthRepository
import com.rango.delivery.data.auth.DefaultSessionRepository
import com.rango.delivery.data.auth.SessionRepository
import com.rango.delivery.data.cart.CartApi
import com.rango.delivery.data.cart.CartRepository
import com.rango.delivery.data.cart.DefaultCartRepository
import com.rango.delivery.data.catalog.CatalogApi
import com.rango.delivery.data.catalog.CatalogRepository
import com.rango.delivery.data.catalog.DefaultCatalogRepository
import com.rango.delivery.data.search.DefaultSearchRepository
import com.rango.delivery.data.search.SearchApi
import com.rango.delivery.data.search.SearchRepository
import com.rango.delivery.data.storage.SecureStorage

class AppContainer(secureStorage: SecureStorage) {
    private val httpClient = createHttpClient()
    private val authApi = AuthApi(httpClient)

    val sessionRepository: SessionRepository = DefaultSessionRepository(authApi, secureStorage)
    val authRepository: AuthRepository = DefaultAuthRepository(authApi, sessionRepository)

    private val authorizedCaller = AuthorizedCaller(sessionRepository)

    val catalogRepository: CatalogRepository = DefaultCatalogRepository(CatalogApi(httpClient), authorizedCaller)
    val cartRepository: CartRepository = DefaultCartRepository(CartApi(httpClient), authorizedCaller)
    val searchRepository: SearchRepository = DefaultSearchRepository(SearchApi(httpClient), authorizedCaller)
}
