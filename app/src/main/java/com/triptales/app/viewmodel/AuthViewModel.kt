package com.triptales.app.viewmodel

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
            tokenManager.clearIfExpired()           // pulisce token se scaduto
            refreshAccessTokenIfNeeded()            // rinnova access_token se necessario
            tokenManager.accessToken.collect { token ->
                _authState.value = if (token != null) AuthState.Authenticated else AuthState.Unauthenticated
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
                    tokenManager.saveTokens(tokens.access, tokens.refresh) // salva i token
                    _authState.value = AuthState.SuccessLogin(tokens)
                } else {
                    _authState.value = AuthState.Error("Credenziali non valide")
                }
            } catch (e: Exception) {
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
                    _authState.value = AuthState.SuccessRegister
                } else {
                    _authState.value = AuthState.Error("Registrazione fallita")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Errore: ${e.message}")
            }
        }
    }

    fun refreshAccessTokenIfNeeded() {
        viewModelScope.launch {
            val access = tokenManager.accessToken.first()
            val refresh = tokenManager.refreshToken.first()

            if (tokenManager.isTokenExpired(access)) {
                if (!tokenManager.isTokenExpired(refresh)) {
                    try {
                        val response = repository.refreshToken(refresh!!)
                        if (response.isSuccessful) {
                            val newTokens = response.body()!!
                            tokenManager.saveTokens(newTokens.access, refresh) // aggiorna solo access
                            _authState.value = AuthState.Authenticated
                        } else {
                            logout() // refresh token non valido
                        }
                    } catch (_: Exception) {
                        logout()
                    }
                } else {
                    logout() // refresh token scaduto
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearTokens()
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}