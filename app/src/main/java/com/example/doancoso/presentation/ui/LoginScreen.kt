package com.example.doancoso.presentation.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController


@Composable
fun LoginScreen(navController: NavHostController) {
    val viewModel: AuthViewModel = viewModel()
    val authState by viewModel.authState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // LaunchedEffect để xử lý logic điều hướng một lần
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.UserLoggedIn -> {
                val user = (authState as AuthState.UserLoggedIn).user
                Toast.makeText(navController.context, "Welcome, ${user?.name}!", Toast.LENGTH_SHORT).show()
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true } // Xóa màn hình login khỏi back stack
                }
                viewModel.resetAuthState() // Reset trạng thái sau khi điều hướng
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (authState) {
            is AuthState.Loading -> CircularProgressIndicator()
            is AuthState.Error -> {
                val error = (authState as AuthState.Error).message
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loginUser(email, password) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Login")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { navController.navigate("signup") }) {
                        Text("Don't have an account? Sign up")
                    }
                }
            }
        }
    }
}