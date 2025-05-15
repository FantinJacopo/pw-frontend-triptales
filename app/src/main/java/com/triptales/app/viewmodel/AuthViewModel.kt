package com.triptales.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.triptales.app.data.auth.AuthRepository
import com.triptales.app.data.auth.LoginRequest
import com.triptales.app.data.auth.LoginResponse
import com.triptales.app.data.auth.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class SuccessLogin(val token: LoginResponse) : AuthState()
    object SuccessRegister : AuthState()
    data class Error(val message: String) : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
}

class AuthViewModel(
    private val repository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    init {
        viewModelScope.launch {
            tokenManager.clearIfExpired()
            checkAuthStatus()

            // Monitora i cambiamenti dei token
            tokenManager.accessToken.collect { token ->
                if (token != null && !tokenManager.isTokenExpired(token)) {
                    Log.d("AuthViewModel", "User is authenticated with valid token")
                    _authState.value = AuthState.Authenticated
                } else {
                    Log.d("AuthViewModel", "User is not authenticated")
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    private suspend fun checkAuthStatus() {
        val accessToken = tokenManager.accessToken.first()
        val refreshToken = tokenManager.refreshToken.first()

        when {
            !accessToken.isNullOrBlank() && !tokenManager.isTokenExpired(accessToken) -> {
                Log.d("AuthViewModel", "Valid access token found")
                _authState.value = AuthState.Authenticated
            }
            !refreshToken.isNullOrBlank() && !tokenManager.isTokenExpired(refreshToken) -> {
                Log.d("AuthViewModel", "Attempting to refresh token...")
                val refreshSuccess = tokenManager.refreshAccessToken()
                if (refreshSuccess) {
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            }
            else -> {
                Log.d("AuthViewModel", "No valid tokens found")
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = repository.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val tokens = response.body()!!
                    tokenManager.saveTokens(tokens.access, tokens.refresh)
                    Log.d("AuthViewModel", "Login successful, tokens saved")
                    _authState.value = AuthState.SuccessLogin(tokens)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AuthViewModel", "Login failed: ${response.code()} - $errorBody")
                    _authState.value = AuthState.Error("Credenziali non valide")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login error", e)
                _authState.value = AuthState.Error("Errore: ${e.message}")
            }
        }
    }

    fun register(email: String, username: String, name: String, password: String, imageFile: File) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("profile_image", imageFile.name, requestFile)

                val response = repository.register(email, username, name, password, imagePart)
                if (response.isSuccessful) {
                    Log.d("AuthViewModel", "Registration successful")
                    _authState.value = AuthState.SuccessRegister
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AuthViewModel", "Registration failed: ${response.code()} - $errorBody")
                    _authState.value = AuthState.Error("Registrazione fallita")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Registration error", e)
                _authState.value = AuthState.Error("Errore: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            Log.d("AuthViewModel", "Logging out...")
            tokenManager.clearTokens()
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}