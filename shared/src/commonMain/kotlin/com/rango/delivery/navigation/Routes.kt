package com.rango.delivery.navigation

import kotlinx.serialization.Serializable

@Serializable
data object WelcomeRoute

@Serializable
data object LoginRoute

@Serializable
data object SignupRoute

@Serializable
data object HomeRoute

@Serializable
data class SearchRoute(val homeSection: String? = null)

@Serializable
data object OrdersRoute

@Serializable
data object FavoritesRoute

@Serializable
data object ProfileRoute

@Serializable
data object CartRoute

@Serializable
data object CheckoutRoute

@Serializable
data object NotificationsRoute

@Serializable
data object AddressRoute

@Serializable
data object PaymentRoute

@Serializable
data class ConfirmedRoute(val orderCode: String)

@Serializable
data class TrackingRoute(val orderCode: String)

@Serializable
data class OrderDetailRoute(val orderCode: String)

@Serializable
data class ProductRoute(val productId: String)

@Serializable
data object EditProfileRoute

@Serializable
data object SettingsRoute

@Serializable
data object ThemeRoute

@Serializable
data object NotificationSettingsRoute
