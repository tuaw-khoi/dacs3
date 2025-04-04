package com.example.doancoso.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.doancoso.data.repository.FirebaseService
import com.example.doancoso.domain.preferences.UserPreferences

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Signup : Screen("signup")
    data object Home : Screen("home")
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val firebaseService = remember { FirebaseService() }

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(userPreferences, firebaseService)
    )
    authViewModel.clearCache()

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(navController, authViewModel)
        }
        composable(Screen.Signup.route) {
            SignupScreen(navController, authViewModel)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController, authViewModel)
        }
    }
}


