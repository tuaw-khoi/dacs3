package com.example.doancoso.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.doancoso.ui.theme.DoancosoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoancosoTheme { // Đảm bảo gọi đúng chủ đề
                AppNavigation()
            }
        }
    }
}

