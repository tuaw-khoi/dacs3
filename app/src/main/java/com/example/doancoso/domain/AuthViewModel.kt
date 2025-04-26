package com.example.doancoso.presentation.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.doancoso.data.models.User
import com.example.doancoso.data.repository.FirebaseService
import com.example.doancoso.domain.preferences.UserPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userPreferences: UserPreferences,
    private val firebaseService: FirebaseService
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    init {
        Log.d("AuthDebug", "ViewModel khởi tạo - AuthState: ${authState.value}")
        loadSavedUser()
    }

    private fun loadSavedUser() {
        viewModelScope.launch {
            val uid = userPreferences.userUidFlow.firstOrNull()
            val name = userPreferences.userNameFlow.firstOrNull()
            val email = userPreferences.userEmailFlow.firstOrNull()

            if (!uid.isNullOrEmpty() && !name.isNullOrEmpty() && !email.isNullOrEmpty()) {
                val savedUser = User(uid = uid, name = name, email = email)
                _user.value = savedUser
                _authState.value = AuthState.UserLoggedIn(savedUser)
            }
        }
    }

    fun registerUser(email: String, password: String, name: String) {
        _authState.value = AuthState.Loading
        firebaseService.registerUser(email, password, name) { success, error ->
            if (success) {
                val uid = firebaseService.auth.currentUser?.uid ?: return@registerUser
                val newUser = User(uid = uid, name = name, email = email)
                _user.value = newUser
                _authState.value = AuthState.UserLoggedIn(newUser)

                // Lưu thông tin người dùng vào SharedPreferences
                viewModelScope.launch {
                    userPreferences.saveUser(uid = uid, name = name, email = email)
                }
            } else {
                _authState.value = AuthState.Error(error ?: "Đăng ký thất bại")
            }
        }
    }

    fun loginUser(email: String, password: String) {
        Log.e("user", "user : $email")
        _authState.value = AuthState.Loading
        firebaseService.loginUser(email, password) { success, error, user ->
            if (success && user != null) {
                Log.e("user", "user : $user")
                _user.value = user
                _authState.value = AuthState.UserLoggedIn(user)
                Log.e("noti", " _authState.value : $authState")
                // Lưu thông tin người dùng vào SharedPreferences
                viewModelScope.launch {
                    userPreferences.saveUser(
                        uid = user.uid,
                        name = user.name,
                        email = user.email
                    )
                }
            } else {
                _authState.value = AuthState.Error(error ?: "Đăng nhập thất bại")
            }
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    fun logoutUser() {
        firebaseService.logout()
        _authState.value = AuthState.Idle
        _user.value = null

        viewModelScope.launch {
            userPreferences.clearUser()
        }
    }

    fun clearCache() {
        _user.value = null
        _authState.value = AuthState.Idle

        // Xóa thông tin người dùng khỏi UserPreferences
        viewModelScope.launch {
            userPreferences.clearUser()
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class UserLoggedIn(val user: User?) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModelFactory(
    private val userPreferences: UserPreferences,
    private val firebaseService: FirebaseService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(userPreferences, firebaseService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
