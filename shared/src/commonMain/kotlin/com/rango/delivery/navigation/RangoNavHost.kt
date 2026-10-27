package com.rango.delivery.navigation

import androidx.compose.runtime.Composable
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
import com.rango.delivery.feature.payment.PaymentRoute
import com.rango.delivery.feature.payment.PaymentViewModel
import com.rango.delivery.feature.product.ProductRoute
import com.rango.delivery.feature.product.ProductViewModel
import com.rango.delivery.feature.search.SearchFilters
import com.rango.delivery.feature.search.SearchRoute
import com.rango.delivery.feature.search.SearchViewModel
import com.rango.delivery.di.AppContainer
import com.rango.delivery.feature.welcome.WelcomeRoute
import com.rango.delivery.feature.welcome.WelcomeViewModel
import com.rango.delivery.core.designsystem.component.RangoTab
import com.rango.delivery.feature.home.HomeActions
import com.rango.delivery.feature.home.HomeRoute
import com.rango.delivery.feature.home.HomeViewModel
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.route_checkout
import com.rango.delivery.resources.route_favorites
import com.rango.delivery.resources.route_notifications
import com.rango.delivery.resources.payment_title
import com.rango.delivery.resources.route_orders
import com.rango.delivery.resources.route_profile
import com.rango.delivery.feature.login.LoginRoute
import com.rango.delivery.feature.login.LoginViewModel
import com.rango.delivery.feature.signup.SignupRoute
import com.rango.delivery.feature.signup.SignupViewModel
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun RangoNavHost(container: AppContainer) {
    val navController = rememberNavController()

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
        pending<OrdersRoute>(Res.string.route_orders, navController::popBackStack)
        pending<FavoritesRoute>(Res.string.route_favorites, navController::popBackStack)
        pending<ProfileRoute>(Res.string.route_profile, navController::popBackStack)
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
            PendingScreen(
                title = stringResource(Res.string.route_checkout),
                onBack = { navController.popBackStack() },
                shortcutText = stringResource(Res.string.payment_title),
                onShortcut = { navController.navigate(PaymentRoute) },
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

private fun NavHostController.navigateToWelcome() {
    navigate(WelcomeRoute) {
        popUpTo(HomeRoute) { inclusive = true }
    }
}

private inline fun <reified T : Any> NavGraphBuilder.pending(title: StringResource, noinline onBack: () -> Unit) {
    composable<T> { PendingScreen(stringResource(title), onBack = { onBack() }) }
}
