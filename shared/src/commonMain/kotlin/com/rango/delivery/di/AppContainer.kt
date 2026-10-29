package com.rango.delivery.di

import com.rango.delivery.core.network.createHttpClient
import com.rango.delivery.data.address.AddressApi
import com.rango.delivery.data.address.AddressRepository
import com.rango.delivery.data.address.DefaultAddressRepository
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
import com.rango.delivery.data.order.DefaultOrderRepository
import com.rango.delivery.data.order.OrderApi
import com.rango.delivery.data.order.OrderRepository
import com.rango.delivery.data.payment.DefaultPaymentRepository
import com.rango.delivery.data.payment.PaymentApi
import com.rango.delivery.data.payment.PaymentRepository
import com.rango.delivery.data.payment.PaymentSelection
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
    val addressRepository: AddressRepository = DefaultAddressRepository(AddressApi(httpClient), authorizedCaller)
    val searchRepository: SearchRepository = DefaultSearchRepository(SearchApi(httpClient), authorizedCaller)
    val paymentRepository: PaymentRepository = DefaultPaymentRepository(PaymentApi(httpClient), authorizedCaller)
    val paymentSelection = PaymentSelection()
    val orderRepository: OrderRepository = DefaultOrderRepository(OrderApi(httpClient), authorizedCaller)
}
