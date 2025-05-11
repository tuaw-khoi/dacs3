package com.example.doancoso.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doancoso.domain.AuthViewModel
import com.example.doancoso.domain.ThemeViewModel
import com.example.doancoso.ui.theme.DoancosoTheme
import android.net.Uri
import android.util.Log
import androidx.navigation.compose.rememberNavController
import com.example.doancoso.data.repository.FirebaseService
import com.example.doancoso.domain.AuthViewModelFactory
import com.example.doancoso.domain.preferences.UserPreferences

class MainActivity : ComponentActivity() {



    private val themeViewModel: ThemeViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            UserPreferences(this),
            FirebaseService()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            // State để lưu planId nếu nhận được từ link
            val deepLinkPlanId = remember { mutableStateOf<String?>(null) }

            // Xử lý link động khi app được mở
            intent?.data?.let { uri ->
                Log.d("MainActivity", "Intent data uri: $uri")
                if (uri.host == "doancoso.com" && uri.path?.startsWith("/plan") == true) {
                    val planId = uri.getQueryParameter("planId")
                    val uid = uri.getQueryParameter("uid") // Lấy thêm uid từ query parameter
                    Log.d("MainActivity", "Extracted planId: $planId, uid: $uid")

                    if (!planId.isNullOrEmpty() && !uid.isNullOrEmpty()) {
                        // Lưu planId và uid vào state hoặc ViewModel nếu cần
                        deepLinkPlanId.value = planId
                        authViewModel.pendingDeepLinkPlanId = planId
                        authViewModel.pendingDeepLinkUid = uid // Lưu thêm ui
                    }
                }
            }


            DoancosoTheme(darkTheme = isDarkTheme) {
                AppNavigation(
                    navController = navController,
                    themeViewModel = themeViewModel,
                    authViewModel = authViewModel,
                    deepLinkPlanId = deepLinkPlanId.value
                )
            }
        }
    }
}

