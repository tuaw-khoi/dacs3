package com.example.doancoso.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.doancoso.data.models.DestinationDetails
import com.example.doancoso.data.remote.ApiClient
import com.example.doancoso.data.remote.PlanService
import com.example.doancoso.data.repository.FirebaseService
import com.example.doancoso.data.repository.PlanRepository
import com.example.doancoso.domain.PlanViewModel
import com.example.doancoso.domain.factory.PlanViewModelFactory
import com.example.doancoso.domain.preferences.UserPreferences

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Signup : Screen("signup")
    data object Home : Screen("home")
    data object Setting : Screen("setting")
    data object SearchPlan : Screen("searchPlan")
    data object Plan : Screen("plan")
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

    val planService = remember { ApiClient.retrofit.create(PlanService::class.java) }
    val planRepository = remember { PlanRepository(planService,firebaseService) }

    val planViewModel: PlanViewModel = viewModel(
        factory = PlanViewModelFactory(planRepository)
    )


    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(navController, authViewModel)
        }
        composable(Screen.Signup.route) {
            SignupScreen(navController, authViewModel)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController, authViewModel, planViewModel)
        }
        composable(Screen.Setting.route) {
            SettingScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(Screen.SearchPlan.route) {
            SearchPlanScreen(navController = navController, authViewModel = authViewModel,planViewModel = planViewModel)
        }
        composable(Screen.Plan.route) {
            PlanScreen(navController = navController, authViewModel = authViewModel,planViewModel = planViewModel)
        }
        composable(
            "planDetail/{planId}"
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId") ?: ""
            PlanDetailScreen(
                navController = navController,
                authViewModel = authViewModel,
                planId = planId,
                planViewModel = planViewModel
            )
        }

        composable("destinationDetail/{destinationName}") { backStackEntry ->
            val destinationName = backStackEntry.arguments?.getString("destinationName") ?: ""
            // Fetch destination details based on destinationName
            DestinationDetailScreen(
                navController = navController,
                authViewModel = authViewModel,
                destination = destinationName,
                planViewModel = planViewModel)
        }
    }
}


