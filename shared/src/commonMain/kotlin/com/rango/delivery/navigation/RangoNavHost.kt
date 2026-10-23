package com.rango.delivery.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rango.delivery.di.AppContainer
import com.rango.delivery.feature.welcome.WelcomeRoute
import com.rango.delivery.feature.welcome.WelcomeViewModel
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.route_home
import com.rango.delivery.resources.route_login
import com.rango.delivery.feature.signup.SignupRoute
import com.rango.delivery.feature.signup.SignupViewModel
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
            PendingScreen(stringResource(Res.string.route_login), onBack = navController::popBackStack)
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
            PendingScreen(stringResource(Res.string.route_home), onBack = null)
        }
    }
}
