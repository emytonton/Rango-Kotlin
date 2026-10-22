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
data object NotificationsRoute

@Serializable
data object AddressRoute

@Serializable
data class ProductRoute(val productId: String)
