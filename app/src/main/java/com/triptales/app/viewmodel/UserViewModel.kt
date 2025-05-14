package com.triptales.app.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.triptales.app.data.user.UserProfile
import com.triptales.app.data.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class UserState {
    object Idle : UserState()
    object Loading : UserState()
    data class Success(val profile: UserProfile) : UserState()
    data class Error(val message: String) : UserState()
}

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    private val _userState = MutableStateFlow<UserState>(UserState.Idle)
    val userState: StateFlow<UserState> = _userState

    fun fetchUserProfile() {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                val response = repository.getUserProfile()
                if (response.isSuccessful) {
                    _userState.value = UserState.Success(response.body()!!)
                } else {
                    _userState.value = UserState.Error("Errore caricamento profilo: ${response.code()}")
                }
            } catch (e: Exception) {
                _userState.value = UserState.Error("Errore: ${e.message}")
            }
        }
    }
}
