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

    // Callback per resettare i dati quando l'utente cambia
    private var onUserDataReset: (() -> Unit)? = null

    /**
     * Imposta la callback per resettare i dati dell'utente.
     * Dovrebbe essere chiamata dall'Activity/Fragment principale.
     */
    fun setUserDataResetCallback(callback: () -> Unit) {
        onUserDataReset = callback
    }

    init {
        // Controlla immediatamente l'autenticazione senza creare cambiamenti di stato extra
        checkInitialAuthStatus()
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

    private fun checkInitialAuthStatus() {
        viewModelScope.launch {
            try {
                // Pulisce token scaduti
                tokenManager.clearIfExpired()

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
                            // Token refresh fallito, reset dei dati utente (se la callback è impostata)
                            onUserDataReset?.invoke()
                            _authState.value = AuthState.Unauthenticated
                        }
                    }
                    else -> {
                        Log.d("AuthViewModel", "No valid tokens found")
                        // Nessun token valido, reset dei dati utente (se la callback è impostata)
                        onUserDataReset?.invoke()
                        _authState.value = AuthState.Unauthenticated
                    }
                }

                // Monitora i cambiamenti dei token
                tokenManager.accessToken.collect { token ->
                    val currentState = _authState.value
                    val shouldBeAuthenticated = !token.isNullOrBlank() && !tokenManager.isTokenExpired(token)

                    if (shouldBeAuthenticated && currentState !is AuthState.Authenticated) {
                        Log.d("AuthViewModel", "User authenticated via token change")
                        _authState.value = AuthState.Authenticated
                    } else if (!shouldBeAuthenticated && currentState is AuthState.Authenticated) {
                        Log.d("AuthViewModel", "User unauthenticated via token change")
                        // Reset dei dati quando l'utente viene disconnesso (se la callback è impostata)
                        onUserDataReset?.invoke()
                        _authState.value = AuthState.Unauthenticated
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error during initial auth check", e)
                onUserDataReset?.invoke()
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // IMPORTANTE: Reset dei dati dell'utente precedente prima del nuovo login (se la callback è impostata)
                onUserDataReset?.invoke()

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

    fun logout() {
        viewModelScope.launch {
            Log.d("AuthViewModel", "Logging out...")
            _authState.value = AuthState.Loading

            // Piccola pausa per feedback visivo
            kotlinx.coroutines.delay(500)

            // IMPORTANTE: Reset dei dati utente PRIMA di pulire i token (se la callback è impostata)
            onUserDataReset?.invoke()

            tokenManager.clearTokens()
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun resetState() {
        // Solo resetta se non stiamo in uno stato di autenticazione
        if (_authState.value !is AuthState.Authenticated && _authState.value !is AuthState.Unauthenticated) {
            _authState.value = AuthState.Idle
        }
    }
}