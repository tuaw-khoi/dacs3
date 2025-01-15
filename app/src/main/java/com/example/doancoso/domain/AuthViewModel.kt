package com.example.doancoso.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancoso.data.models.User
import com.example.doancoso.data.repository.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val firebaseService = FirebaseService()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun registerUser(email: String, password: String, name: String) {
        _authState.value = AuthState.Loading
        firebaseService.registerUser(email, password, name) { success, error ->
            if (success) {
                _authState.value = AuthState.Success("Registration successful!")
            } else {
                _authState.value = AuthState.Error(error ?: "An unknown error occurred")
            }
        }
    }

    fun loginUser(email: String, password: String) {
        _authState.value = AuthState.Loading
        firebaseService.loginUser(email, password) { success, error, user ->
            if (success) {
                _authState.value = AuthState.UserLoggedIn(user)
            } else {
                _authState.value = AuthState.Error(error ?: "Login failed")
            }
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    fun logoutUser() {
        firebaseService.logout() // Xóa thông tin đăng nhập trong Firebase (nếu có)
        _authState.value = AuthState.Idle // Đặt trạng thái lại thành Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class UserLoggedIn(val user: User?) : AuthState()
    data class Error(val message: String) : AuthState()
}
