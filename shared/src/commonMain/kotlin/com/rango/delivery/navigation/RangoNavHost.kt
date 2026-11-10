package com.rango.delivery.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.rango.delivery.feature.address.AddressRoute
import com.rango.delivery.feature.address.AddressViewModel
import com.rango.delivery.feature.cart.CartActions
import com.rango.delivery.feature.cart.CartRoute
import com.rango.delivery.feature.cart.CartViewModel
import com.rango.delivery.feature.checkout.CheckoutNavigation
import com.rango.delivery.feature.checkout.CheckoutRoute
import com.rango.delivery.feature.checkout.CheckoutViewModel
import com.rango.delivery.feature.confirmed.ConfirmedRoute
import com.rango.delivery.feature.confirmed.ConfirmedViewModel
import com.rango.delivery.feature.orderdetail.OrderDetailRoute
import com.rango.delivery.feature.orderdetail.OrderDetailViewModel
import com.rango.delivery.feature.favorites.FavoritesNavigation
import com.rango.delivery.feature.favorites.FavoritesRoute
import com.rango.delivery.feature.favorites.FavoritesViewModel
import com.rango.delivery.feature.orders.OrdersNavigation
import com.rango.delivery.feature.orders.OrdersRoute
import com.rango.delivery.feature.orders.OrdersViewModel
import com.rango.delivery.feature.payment.PaymentRoute
import com.rango.delivery.feature.payment.PaymentViewModel
import com.rango.delivery.feature.product.ProductRoute
import com.rango.delivery.feature.profile.ProfileNavigation
import com.rango.delivery.feature.editprofile.EditProfileRoute
import com.rango.delivery.feature.editprofile.EditProfileViewModel
import com.rango.delivery.feature.profile.ProfileRoute
import com.rango.delivery.feature.profile.ProfileViewModel
import com.rango.delivery.feature.product.ProductViewModel
import com.rango.delivery.feature.search.SearchFilters
import com.rango.delivery.feature.search.SearchRoute
import com.rango.delivery.feature.search.SearchViewModel
import com.rango.delivery.feature.settings.SettingsNavigation
import com.rango.delivery.feature.settings.SettingsRoute
import com.rango.delivery.feature.settings.SettingsViewModel
import com.rango.delivery.feature.theme.ThemeRoute
import com.rango.delivery.feature.theme.ThemeViewModel
import com.rango.delivery.feature.tracking.TrackingRoute
import com.rango.delivery.feature.tracking.TrackingViewModel
import com.rango.delivery.di.AppContainer
import com.rango.delivery.feature.welcome.WelcomeRoute
import com.rango.delivery.feature.welcome.WelcomeViewModel
import com.rango.delivery.core.designsystem.component.LocalTabBadges
import com.rango.delivery.core.designsystem.component.RangoTab
import com.rango.delivery.data.favorite.FavoriteRepository
import kotlinx.coroutines.CancellationException
import com.rango.delivery.feature.home.HomeActions
import com.rango.delivery.feature.home.HomeRoute
import com.rango.delivery.feature.home.HomeViewModel
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.route_notifications
import com.rango.delivery.resources.route_notification_settings
import com.rango.delivery.feature.login.LoginRoute
import com.rango.delivery.feature.login.LoginViewModel
import com.rango.delivery.feature.signup.SignupRoute
import com.rango.delivery.feature.signup.SignupViewModel
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun RangoNavHost(container: AppContainer) {
    val navController = rememberNavController()
    val favoriteIds by container.favoriteRepository.favoriteIds.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalTabBadges provides mapOf(RangoTab.Favorites to favoriteIds.size)) {
        RangoNavGraph(container, navController)
    }
}

@Composable
private fun RangoNavGraph(container: AppContainer, navController: NavHostController) {
    NavHost(navController = navController, startDestination = WelcomeRoute) {
        composable<WelcomeRoute> {
            WelcomeRoute(
                viewModel = viewModel { WelcomeViewModel(container.sessionRepository) },
                onLogin = { navController.navigate(LoginRoute) },
                onSignup = { navController.navigate(SignupRoute) },
                onSessionRestored = {
                    navController.navigate(HomeRoute) {
                        popUpTo(WelcomeRoute) { inclusive = true }
                    }
                },
            )
        }
        composable<LoginRoute> {
            LoginRoute(
                viewModel = viewModel { LoginViewModel(container.authRepository) },
                onBack = navController::popBackStack,
                onSignup = {
                    navController.navigate(SignupRoute) {
                        popUpTo(WelcomeRoute)
                    }
                },
                onLoggedIn = {
                    navController.navigate(HomeRoute) {
                        popUpTo(WelcomeRoute) { inclusive = true }
                    }
                },
            )
        }
        composable<SignupRoute> {
            SignupRoute(
                viewModel = viewModel { SignupViewModel(container.authRepository) },
                onBack = navController::popBackStack,
                onLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo(WelcomeRoute)
                    }
                },
                onRegistered = {
                    navController.navigate(HomeRoute) {
                        popUpTo(WelcomeRoute) { inclusive = true }
                    }
                },
            )
        }
        composable<HomeRoute> {
            LaunchedEffect(Unit) { container.favoriteRepository.syncQuietly() }
            HomeRoute(
                viewModel = viewModel { HomeViewModel(container.catalogRepository, container.cartRepository) },
                navigation = HomeActions(
                    onOpenProduct = { navController.navigate(ProductRoute(it)) },
                    onSeeAll = { navController.navigate(SearchRoute(homeSection = it)) },
                    onProfile = { navController.navigate(ProfileRoute) },
                    onAddress = { navController.navigate(AddressRoute) },
                    onNotifications = { navController.navigate(NotificationsRoute) },
                    onCart = { navController.navigate(CartRoute) },
                    onSearch = { navController.navigate(SearchRoute()) },
                    onTab = navController::navigateToTab,
                ),
                onSessionExpired = navController::navigateToWelcome,
            )
        }
        composable<SearchRoute> { entry ->
            val route = entry.toRoute<SearchRoute>()
            SearchRoute(
                viewModel = viewModel {
                    SearchViewModel(
                        searchRepository = container.searchRepository,
                        catalogRepository = container.catalogRepository,
                        cartRepository = container.cartRepository,
                        initialFilters = SearchFilters.forHomeSection(route.homeSection),
                    )
                },
                onOpenProduct = { navController.navigate(ProductRoute(it)) },
                onTab = navController::navigateToTab,
                onSessionExpired = navController::navigateToWelcome,
            )
        }
        composable<OrdersRoute> {
            OrdersRoute(
                viewModel = viewModel { OrdersViewModel(container.orderRepository) },
                navigation = OrdersNavigation(
                    onOpenOrder = { navController.navigate(OrderDetailRoute(it)) },
                    onTrack = { navController.navigate(TrackingRoute(it)) },
                    onCart = { navController.navigate(CartRoute) },
                    onHome = { navController.popBackStack(HomeRoute, inclusive = false) },
                    onTab = navController::navigateToTab,
                ),
                onSessionExpired = navController::navigateToWelcome,
            )
        }
        composable<OrderDetailRoute> { entry ->
            val route = entry.toRoute<OrderDetailRoute>()
            OrderDetailRoute(
                viewModel = viewModel { OrderDetailViewModel(route.orderCode, container.orderRepository) },
                orderCode = route.orderCode,
                onBack = navController::popBackStack,
                onCart = { navController.navigate(CartRoute) },
                onSessionExpired = navController::navigateToWelcome,
            )
        }
        composable<FavoritesRoute> {
            FavoritesRoute(
                viewModel = viewModel { FavoritesViewModel(container.favoriteRepository, container.cartRepository) },
                navigation = FavoritesNavigation(
                    onOpenProduct = { navController.navigate(ProductRoute(it)) },
                    onExplore = { navController.popBackStack(HomeRoute, inclusive = false) },
                    onTab = navController::navigateToTab,
                ),
                onSessionExpired = navController::navigateToWelcome,
            )
        }
        composable<ProfileRoute> {
            ProfileRoute(
                viewModel = viewModel {
                    ProfileViewModel(
                        profileRepository = container.profileRepository,
                        addressRepository = container.addressRepository,
                        paymentRepository = container.paymentRepository,
                        sessionRepository = container.sessionRepository,
                        themeRepository = container.themeRepository,
                    )
                },
                navigation = ProfileNavigation(
                    onNotifications = { navController.navigate(NotificationsRoute) },
                    onEditProfile = { navController.navigate(EditProfileRoute) },
                    onAddresses = { navController.navigate(AddressRoute) },
                    onPayments = { navController.navigate(PaymentRoute) },
                    onSettings = { navController.navigate(SettingsRoute) },
                    onTheme = { navController.navigate(ThemeRoute) },
                    onTab = navController::navigateToTab,
                ),
                onLoggedOut = navController::navigateToWelcome,
                onSessionExpired = navController::navigateToWelcome,
            )
        }
        composable<EditProfileRoute> {
            EditProfileRoute(
                viewModel = viewModel { EditProfileViewModel(container.profileRepository) },
                onBack = navController::popBackStack,
                onSessionExpired = navController::navigateToWelcome,
            )
        }
        composable<SettingsRoute> {
            SettingsRoute(
                viewModel = viewModel {
                    SettingsViewModel(
                        profileRepository = container.profileRepository,
                        connectionRepository = container.connectionRepository,
                        dataSync = container.dataSync,
                        sessionRepository = container.sessionRepository,
                        favoriteRepository = container.favoriteRepository,
                        paymentSelection = container.paymentSelection,
                        themeRepository = container.themeRepository,
                    )
                },
                navigation = SettingsNavigation(
                    onBack = navController::popBackStack,
                    onTheme = { navController.navigate(ThemeRoute) },
                    onNotifications = { navController.navigate(NotificationSettingsRoute) },
                    onPersonalData = { navController.navigate(EditProfileRoute) },
                    onAddresses = { navController.navigate(AddressRoute) },
                    onPayments = { navController.navigate(PaymentRoute) },
                ),
                onLocalDataCleared = navController::navigateToWelcome,
                onSessionExpired = navController::navigateToWelcome,
            )
        }
        pending<NotificationSettingsRoute>(Res.string.route_notification_settings, navController::popBackStack)
        composable<ThemeRoute> {
            ThemeRoute(
                viewModel = viewModel { ThemeViewModel(container.themeRepository) },
                onBack = navController::popBackStack,
                onSessionExpired = navController::navigateToWelcome,
            )
        }
        composable<CartRoute> {
            CartRoute(
                viewModel = viewModel { CartViewModel(container.cartRepository) },
                navigation = CartActions(
                    onBack = navController::popBackStack,
                    onOpenProduct = { navController.navigate(ProductRoute(it)) },
                    onCheckout = { navController.navigate(CheckoutRoute) },
                    onContinueShopping = { navController.popBackStack(HomeRoute, inclusive = false) },
                ),
                onSessionExpired = navController::navigateToWelcome,
            )
        }
        composable<CheckoutRoute> {
            CheckoutRoute(
                viewModel = viewModel {
                    CheckoutViewModel(
                        cartRepository = container.cartRepository,
                        addressRepository = container.addressRepository,
                        paymentRepository = container.paymentRepository,
                        paymentSelection = container.paymentSelection,
                        orderRepository = container.orderRepository,
                    )
                },
                navigation = CheckoutNavigation(
                    onBack = navController::popBackStack,
                    onOpenCart = { navController.popBackStack(CartRoute, inclusive = false) },
                    onOpenAddress = { navController.navigate(AddressRoute) },
                    onOpenPayment = { navController.navigate(PaymentRoute) },
                    onOrderPlaced = { code ->
                        navController.navigate(ConfirmedRoute(code)) {
                            popUpTo(HomeRoute)
                        }
                    },
                ),
                onSessionExpired = navController::navigateToWelcome,
            )
        }
        composable<ConfirmedRoute> { entry ->
            val route = entry.toRoute<ConfirmedRoute>()
            ConfirmedRoute(
                viewModel = viewModel { ConfirmedViewModel(route.orderCode, container.orderRepository) },
                onTrack = { navController.navigate(TrackingRoute(route.orderCode)) },
                onHome = { navController.popBackStack(HomeRoute, inclusive = false) },
                onSessionExpired = navController::navigateToWelcome,
            )
        }
        composable<TrackingRoute> { entry ->
            val route = entry.toRoute<TrackingRoute>()
            TrackingRoute(
                viewModel = viewModel { TrackingViewModel(route.orderCode, container.orderRepository) },
                orderCode = route.orderCode,
                onBack = navController::popBackStack,
                onSessionExpired = navController::navigateToWelcome,
            )
        }
        composable<PaymentRoute> {
            PaymentRoute(
                viewModel = viewModel { PaymentViewModel(container.paymentRepository, container.paymentSelection) },
                onBack = navController::popBackStack,
                onSessionExpired = navController::navigateToWelcome,
            )
        }
        pending<NotificationsRoute>(Res.string.route_notifications, navController::popBackStack)
        composable<AddressRoute> {
            AddressRoute(
                viewModel = viewModel { AddressViewModel(container.addressRepository) },
                onBack = navController::popBackStack,
                onSessionExpired = navController::navigateToWelcome,
            )
        }
        composable<ProductRoute> { entry ->
            val route = entry.toRoute<ProductRoute>()
            ProductRoute(
                viewModel = viewModel {
                    ProductViewModel(route.productId, container.catalogRepository, container.cartRepository)
                },
                onBack = navController::popBackStack,
                onSessionExpired = navController::navigateToWelcome,
            )
        }
    }
}

private fun NavHostController.navigateToTab(tab: RangoTab) {
    val route: Any = when (tab) {
        RangoTab.Home -> {
            popBackStack(HomeRoute, inclusive = false)
            return
        }
        RangoTab.Search -> SearchRoute()
        RangoTab.Orders -> OrdersRoute
        RangoTab.Favorites -> FavoritesRoute
        RangoTab.Profile -> ProfileRoute
    }
    navigate(route) {
        popUpTo(HomeRoute)
        launchSingleTop = true
    }
}

private suspend fun FavoriteRepository.syncQuietly() {
    try {
        list()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
    }
}

private fun NavHostController.navigateToWelcome() {
    navigate(WelcomeRoute) {
        popUpTo(HomeRoute) { inclusive = true }
    }
}

private inline fun <reified T : Any> NavGraphBuilder.pending(title: StringResource, noinline onBack: () -> Unit) {
    composable<T> { PendingScreen(stringResource(title), onBack = { onBack() }) }
}
