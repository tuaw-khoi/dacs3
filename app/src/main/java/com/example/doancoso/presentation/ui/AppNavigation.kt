package com.example.doancoso.presentation.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.doancoso.data.models.PlanResultDb
import com.example.doancoso.data.remote.ApiClient
import com.example.doancoso.data.remote.PlanService
import com.example.doancoso.data.repository.FirebaseService
import com.example.doancoso.data.repository.PlanRepository
import com.example.doancoso.domain.AuthState
import com.example.doancoso.domain.AuthViewModel
import com.example.doancoso.domain.AuthViewModelFactory
import com.example.doancoso.domain.PlanUiState
import com.example.doancoso.domain.PlanViewModel
import com.example.doancoso.domain.ThemeViewModel
import com.example.doancoso.domain.factory.PlanViewModelFactory
import com.example.doancoso.domain.preferences.UserPreferences
import com.example.doancoso.presentation.ui.plan.EditDayScreen
import com.example.doancoso.presentation.ui.plan.EditPlanScreen
import com.example.doancoso.presentation.ui.profile.EditProfileScreen
import android.util.Log
import com.example.doancoso.presentation.ui.profile.HelpScreen
import com.example.doancoso.presentation.ui.profile.TermsScreen

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Signup : Screen("signup")
    data object Home : Screen("home")
    data object Setting : Screen("setting")
    data object SearchPlan : Screen("searchPlan")
    data object Plan : Screen("plan")
    data object EditProfile : Screen("editProfile")
    data object Help : Screen("help")
    data object Terms : Screen("terms")

}

@Composable
fun AppNavigation(
    navController: NavHostController,
    themeViewModel: ThemeViewModel,
    authViewModel: AuthViewModel,
    deepLinkPlanId: String? = null
) {
    val context = LocalContext.current
    val firebaseService = remember { FirebaseService() }
    val planService = remember { ApiClient.retrofit.create(PlanService::class.java) }
    val planRepository = remember { PlanRepository(planService, firebaseService) }
    val planViewModel: PlanViewModel = viewModel(factory = PlanViewModelFactory(planRepository))
    val authState by authViewModel.authState.collectAsState()

    // UI điều hướng
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
            SettingScreen(navController, authViewModel, themeViewModel)
        }
        composable(Screen.SearchPlan.route) {
            SearchPlanScreen(navController, authViewModel, planViewModel)
        }
        composable(Screen.Plan.route) {
            PlanScreen(navController, authViewModel, planViewModel)
        }
        composable(Screen.EditProfile.route) {
            EditProfileScreen(navController, authViewModel)
        }
        composable(Screen.Help.route) {
            HelpScreen(navController)
        }
        composable(Screen.Terms.route) {
            TermsScreen(navController)
        }


        composable("planDetail/{planId}") { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId")
            if (planId != null) {
                PlanDetailScreen(
                    planId = planId,
                    navController = navController,
                    authViewModel = authViewModel,
                    planViewModel = planViewModel
                )
            }
        }
        composable("planDetailOwner/{planId}") { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId")
            if (planId != null) {
                PlanDetailOwnerScreen(
                    planId = planId,
                    navController = navController,
                    authViewModel = authViewModel,
                    planViewModel = planViewModel
                )
            }
        }
        composable("destinationDetail/{destinationName}") { backStackEntry ->
            val destinationName = backStackEntry.arguments?.getString("destinationName") ?: ""
            DestinationDetailScreen(navController, authViewModel, destinationName, planViewModel)
        }
        composable("editPlan/{planId}") { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId") ?: ""
            EditPlanScreen(navController, planId, planViewModel, authViewModel)
        }
        composable("editDay/{planId}/{uid}/{dayIndex}") { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId") ?: ""
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            val dayIndex = backStackEntry.arguments?.getString("dayIndex")?.toIntOrNull() ?: 0
            val planState = planViewModel.planState.collectAsState().value
            val plan = (planState as? PlanUiState.Success)?.plan as? PlanResultDb
            if (plan != null) {
                EditDayScreen(dayIndex, planId, uid, planViewModel, navController, plan)
            }
        }
    }

    // Điều hướng tới planDetail nếu có deep link sau khi đăng nhập
    LaunchedEffect(authState, deepLinkPlanId) {
        if (authState is AuthState.UserLoggedIn) {
            val planId = deepLinkPlanId ?: authViewModel.pendingDeepLinkPlanId
            if (!planId.isNullOrEmpty()) {
                kotlinx.coroutines.delay(100) // ⏳ Chờ NavHost dựng xong
                try {
                    navController.navigate("planDetailOwner/$planId") {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                } catch (e: Exception) {
                    Log.e("AppNavigation", "Navigation failed", e)
                }
            }
        }
    }
}


