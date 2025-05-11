package com.example.doancoso.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doancoso.domain.AuthViewModel
import com.example.doancoso.domain.ThemeViewModel
import com.example.doancoso.ui.theme.DoancosoTheme

class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

            DoancosoTheme(darkTheme = isDarkTheme) {
                AppNavigation(themeViewModel = themeViewModel)
            }
        }
    }
}

