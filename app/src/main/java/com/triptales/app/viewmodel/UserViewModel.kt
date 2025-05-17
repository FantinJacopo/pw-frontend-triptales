package com.triptales.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.triptales.app.data.user.UserProfile
import com.triptales.app.data.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

sealed class UserState {
    object Idle : UserState()
    object Loading : UserState()
    data class Success(val profile: UserProfile) : UserState()
    data class Error(val message: String) : UserState()
}

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    private val _userState = MutableStateFlow<UserState>(UserState.Idle)
    val userState: StateFlow<UserState> = _userState

    // Cache per i profili utente, utile per evitare chiamate ripetute
    private val userProfileCache = mutableMapOf<Int, UserProfile>()

    fun fetchUserProfile() {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                val response = repository.getUserProfile()
                if (response.isSuccessful) {
                    val profile = response.body()!!
                    _userState.value = UserState.Success(profile)

                    // Aggiorna la cache con il profilo dell'utente corrente
                    userProfileCache[profile.id] = profile
                } else {
                    _userState.value = UserState.Error("Errore caricamento profilo: ${response.code()}")
                }
            } catch (e: Exception) {
                _userState.value = UserState.Error("Errore: ${e.message}")
            }
        }
    }

    /**
     * Ottiene il profilo di un utente specifico per ID.
     * Prima controlla nella cache, poi fa una chiamata API se necessario.
     *
     * @param userId ID dell'utente di cui si vuole ottenere il profilo
     * @return UserProfile con i dati dell'utente se disponibili, null altrimenti
     * @throws Exception in caso di errore nella comunicazione con il server
     */
    suspend fun fetchUserById(userId: Int): UserProfile? {
        return withContext(Dispatchers.IO) {
            try {
                // Verifica se il profilo è già in cache
                userProfileCache[userId]?.let {
                    Log.d("UserViewModel", "Returning cached profile for user $userId")
                    return@withContext it
                }

                Log.d("UserViewModel", "Fetching profile for user $userId from API")
                val response = repository.getUserById(userId)

                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()!!

                    // Salva nella cache per usi futuri
                    userProfileCache[userId] = profile

                    return@withContext profile
                } else {
                    val errorMsg = "Errore nel caricamento del profilo: ${response.code()}"
                    Log.e("UserViewModel", errorMsg)
                    throw Exception(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error fetching user profile: ${e.message}")
                throw e
            }
        }
    }

    /**
     * Pulisce la cache dei profili utente.
     * Utile quando si vuole forzare un nuovo caricamento.
     */
    fun clearUserProfileCache() {
        userProfileCache.clear()
    }
}