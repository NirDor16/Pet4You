package com.example.pet4you.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pet4you.data.model.UserRole
import com.example.pet4you.ui.auth.LoginScreen
import com.example.pet4you.ui.auth.RegisterScreen
import com.example.pet4you.ui.home.DogOwnerHomeScreen
import com.example.pet4you.ui.home.ServiceProviderHomeScreen
import com.example.pet4you.ui.splash.SplashScreen
import com.example.pet4you.viewmodel.AuthViewModel

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DOG_OWNER_HOME = "dog_owner_home"
    const val SERVICE_PROVIDER_HOME = "service_provider_home"
}

fun homeRouteForRole(role: String): String {
    return when (role) {
        UserRole.SERVICE_PROVIDER -> Routes.SERVICE_PROVIDER_HOME
        else -> Routes.DOG_OWNER_HOME
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.SPLASH) {
            SplashScreen(
                onDestinationReady = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { role ->
                    navController.navigate(homeRouteForRole(role)) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                authViewModel = authViewModel
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { role ->
                    navController.navigate(homeRouteForRole(role)) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                authViewModel = authViewModel
            )
        }

        composable(Routes.DOG_OWNER_HOME) {
            DogOwnerHomeScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DOG_OWNER_HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SERVICE_PROVIDER_HOME) {
            ServiceProviderHomeScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SERVICE_PROVIDER_HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}
