package com.rango.delivery.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rango.delivery.resources.Res
import com.rango.delivery.resources.route_home
import com.rango.delivery.resources.route_login
import com.rango.delivery.resources.route_signup
import com.rango.delivery.resources.route_welcome
import org.jetbrains.compose.resources.stringResource

@Composable
fun RangoNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = WelcomeRoute) {
        composable<WelcomeRoute> {
            PendingScreen(stringResource(Res.string.route_welcome), onBack = null)
        }
        composable<LoginRoute> {
            PendingScreen(stringResource(Res.string.route_login), onBack = navController::popBackStack)
        }
        composable<SignupRoute> {
            PendingScreen(stringResource(Res.string.route_signup), onBack = navController::popBackStack)
        }
        composable<HomeRoute> {
            PendingScreen(stringResource(Res.string.route_home), onBack = null)
        }
    }
}
