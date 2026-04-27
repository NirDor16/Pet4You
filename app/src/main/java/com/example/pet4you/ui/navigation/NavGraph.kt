package com.example.pet4you.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.pet4you.data.model.UserRole
import com.example.pet4you.ui.auth.LoginScreen
import com.example.pet4you.ui.auth.RegisterScreen
import com.example.pet4you.ui.dog.AddEditDogScreen
import com.example.pet4you.ui.dog.DogListScreen
import com.example.pet4you.ui.home.DogOwnerHomeScreen
import com.example.pet4you.ui.home.ServiceProviderHomeScreen
import com.example.pet4you.ui.meetup.CreateMeetupScreen
import com.example.pet4you.ui.meetup.MeetupListScreen
import com.example.pet4you.ui.reminder.AddEditReminderScreen
import com.example.pet4you.ui.reminder.ReminderListScreen
import com.example.pet4you.ui.serviceprovider.ServiceProviderProfileScreen
import com.example.pet4you.ui.splash.SplashScreen
import com.example.pet4you.viewmodel.AuthViewModel

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DOG_OWNER_HOME = "dog_owner_home"
    const val SERVICE_PROVIDER_HOME = "service_provider_home"
    const val DOG_LIST = "dog_list"
    const val ADD_EDIT_DOG = "add_edit_dog?dogId={dogId}"
    const val REMINDER_LIST = "reminder_list"
    const val ADD_EDIT_REMINDER = "add_edit_reminder?reminderId={reminderId}"
    const val MEETUP_LIST = "meetup_list"
    const val CREATE_MEETUP = "create_meetup"
    const val PROVIDER_PROFILE = "provider_profile"
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
                },
                onNavigateToDogs = {
                    navController.navigate(Routes.DOG_LIST)
                },
                onNavigateToReminders = {
                    navController.navigate(Routes.REMINDER_LIST)
                },
                onNavigateToMeetups = {
                    navController.navigate(Routes.MEETUP_LIST)
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
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.PROVIDER_PROFILE)
                }
            )
        }

        composable(Routes.PROVIDER_PROFILE) {
            ServiceProviderProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DOG_LIST) {
            DogListScreen(
                onNavigateToAdd = { navController.navigate("add_edit_dog") },
                onNavigateToEdit = { dogId -> navController.navigate("add_edit_dog?dogId=$dogId") },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.ADD_EDIT_DOG,
            arguments = listOf(
                navArgument("dogId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val dogId = backStackEntry.arguments?.getString("dogId")
            AddEditDogScreen(
                dogId = dogId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.REMINDER_LIST) {
            ReminderListScreen(
                onNavigateToAdd = { navController.navigate("add_edit_reminder") },
                onNavigateToEdit = { reminderId -> navController.navigate("add_edit_reminder?reminderId=$reminderId") },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.ADD_EDIT_REMINDER,
            arguments = listOf(
                navArgument("reminderId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val reminderId = backStackEntry.arguments?.getString("reminderId")
            AddEditReminderScreen(
                reminderId = reminderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MEETUP_LIST) {
            MeetupListScreen(
                onNavigateToCreate = { navController.navigate(Routes.CREATE_MEETUP) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CREATE_MEETUP) {
            CreateMeetupScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
