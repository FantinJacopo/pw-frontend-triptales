package com.triptales.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.triptales.app.data.AuthRepository
import com.triptales.app.data.LoginRequest
import com.triptales.app.data.LoginResponse
import com.triptales.app.data.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val token: LoginResponse) : AuthState()
    data class Error(val message: String) : AuthState()
    object SuccessRegister : AuthState()

}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = repository.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    _authState.value = AuthState.Success(response.body()!!)
                } else {
                    _authState.value = AuthState.Error("Credenziali non valide")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Errore: ${e.message}")
            }
        }
    }
    fun register(email: String, username: String, name: String, profileImageUrl: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val request = RegisterRequest(email, username, name, profileImageUrl, password)
                val response = repository.register(request)
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

}