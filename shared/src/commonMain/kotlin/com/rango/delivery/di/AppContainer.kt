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
import com.rango.delivery.data.cache.LocalCache
import com.rango.delivery.data.cart.CartApi
import com.rango.delivery.data.cart.CartRepository
import com.rango.delivery.data.cart.DefaultCartRepository
import com.rango.delivery.data.catalog.CatalogApi
import com.rango.delivery.data.catalog.CatalogRepository
import com.rango.delivery.data.catalog.DefaultCatalogRepository
import com.rango.delivery.data.connection.ConnectionApi
import com.rango.delivery.data.connection.ConnectionRepository
import com.rango.delivery.data.connection.ConnectivityMonitor
import com.rango.delivery.data.connection.DefaultConnectionRepository
import com.rango.delivery.data.favorite.DefaultFavoriteRepository
import com.rango.delivery.data.favorite.FavoriteApi
import com.rango.delivery.data.favorite.FavoriteRepository
import com.rango.delivery.data.notification.DefaultNotificationRepository
import com.rango.delivery.data.notification.NotificationApi
import com.rango.delivery.data.notification.NotificationRepository
import com.rango.delivery.data.order.DefaultOrderRepository
import com.rango.delivery.data.order.OrderApi
import com.rango.delivery.data.order.OrderRepository
import com.rango.delivery.data.payment.DefaultPaymentRepository
import com.rango.delivery.data.payment.PaymentApi
import com.rango.delivery.data.payment.PaymentRepository
import com.rango.delivery.data.payment.PaymentSelection
import com.rango.delivery.data.profile.DefaultProfileRepository
import com.rango.delivery.data.profile.ProfileApi
import com.rango.delivery.data.profile.ProfileRepository
import com.rango.delivery.data.push.LocalNotifier
import com.rango.delivery.data.push.PushWatcher
import com.rango.delivery.data.search.DefaultSearchRepository
import com.rango.delivery.data.search.SearchApi
import com.rango.delivery.data.search.SearchRepository
import com.rango.delivery.data.storage.SecureStorage
import com.rango.delivery.data.sync.DataSync
import com.rango.delivery.data.theme.ThemeRepository

class AppContainer(
    secureStorage: SecureStorage,
    localNotifier: LocalNotifier,
    val connectivityMonitor: ConnectivityMonitor,
) {
    private val httpClient = createHttpClient()
    private val authApi = AuthApi(httpClient)

    private val localCache = LocalCache(secureStorage)

    val sessionRepository: SessionRepository = DefaultSessionRepository(authApi, secureStorage, onClear = localCache::clear)
    val authRepository: AuthRepository = DefaultAuthRepository(authApi, sessionRepository)

    private val authorizedCaller = AuthorizedCaller(sessionRepository)

    val favoriteRepository: FavoriteRepository =
        DefaultFavoriteRepository(FavoriteApi(httpClient), authorizedCaller, localCache) { catalogRepository.cachedProduct(it) }
    val catalogRepository: CatalogRepository =
        DefaultCatalogRepository(CatalogApi(httpClient), authorizedCaller, favoriteRepository, localCache)
    val cartRepository: CartRepository = DefaultCartRepository(CartApi(httpClient), authorizedCaller, localCache)
    val addressRepository: AddressRepository = DefaultAddressRepository(AddressApi(httpClient), authorizedCaller, localCache)
    val searchRepository: SearchRepository = DefaultSearchRepository(SearchApi(httpClient), authorizedCaller, localCache)
    val paymentRepository: PaymentRepository = DefaultPaymentRepository(PaymentApi(httpClient), authorizedCaller, localCache)
    val paymentSelection = PaymentSelection()
    val orderRepository: OrderRepository = DefaultOrderRepository(OrderApi(httpClient), authorizedCaller, localCache)
    val notificationRepository: NotificationRepository =
        DefaultNotificationRepository(NotificationApi(httpClient), authorizedCaller, localCache)
    val profileRepository: ProfileRepository = DefaultProfileRepository(ProfileApi(httpClient), authorizedCaller, localCache)
    val connectionRepository: ConnectionRepository = DefaultConnectionRepository(ConnectionApi(httpClient))
    val themeRepository = ThemeRepository(secureStorage, profileRepository)
    val dataSync = DataSync(profileRepository, favoriteRepository, cartRepository, themeRepository)
    val pushWatcher = PushWatcher(notificationRepository, sessionRepository, secureStorage, localNotifier)
}
