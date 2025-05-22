// UserViewModel.kt
package com.triptales.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.triptales.app.data.user.UserProfile
import com.triptales.app.data.user.UserRepository
import com.triptales.app.data.user.UserBadge
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

sealed class BadgeState {
    object Idle : BadgeState()
    object Loading : BadgeState()
    data class Success(val badges: List<UserBadge>) : BadgeState()
    data class Error(val message: String) : BadgeState()
}

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    private val _userState = MutableStateFlow<UserState>(UserState.Idle)
    val userState: StateFlow<UserState> = _userState

    private val _badgeState = MutableStateFlow<BadgeState>(BadgeState.Idle)
    val badgeState: StateFlow<BadgeState> = _badgeState

    // Cache per i profili utente, utile per evitare chiamate ripetute
    private val userProfileCache = mutableMapOf<Int, UserProfile>()

    // Cache per i badge utente
    private val userBadgeCache = mutableMapOf<Int, List<UserBadge>>()

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
     * Carica i badge dell'utente corrente.
     */
    fun fetchUserBadges() {
        viewModelScope.launch {
            _badgeState.value = BadgeState.Loading
            try {
                Log.d("UserViewModel", "Fetching user badges")
                val response = repository.getUserBadges()

                if (response.isSuccessful) {
                    val badges = response.body() ?: emptyList()
                    Log.d("UserViewModel", "Successfully fetched ${badges.size} badges")
                    _badgeState.value = BadgeState.Success(badges)
                } else {
                    val errorMsg = "Errore nel caricamento dei badge: ${response.code()}"
                    Log.e("UserViewModel", errorMsg)
                    _badgeState.value = BadgeState.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Exception fetching user badges", e)
                _badgeState.value = BadgeState.Error("Errore: ${e.message}")
            }
        }
    }

    /**
     * Carica i badge di un utente specifico per ID.
     *
     * @param userId ID dell'utente di cui caricare i badge
     */
    fun fetchUserBadges(userId: Int) {
        viewModelScope.launch {
            _badgeState.value = BadgeState.Loading
            try {
                Log.d("UserViewModel", "Fetching badges for user $userId")

                // Verifica se i badge sono già in cache
                userBadgeCache[userId]?.let { cachedBadges ->
                    Log.d("UserViewModel", "Returning cached badges for user $userId")
                    _badgeState.value = BadgeState.Success(cachedBadges)
                    return@launch
                }

                val response = repository.getUserBadges(userId)

                if (response.isSuccessful) {
                    val badges = response.body() ?: emptyList()
                    Log.d("UserViewModel", "Successfully fetched ${badges.size} badges for user $userId")

                    // Salva nella cache
                    userBadgeCache[userId] = badges

                    _badgeState.value = BadgeState.Success(badges)
                } else {
                    val errorMsg = "Errore nel caricamento dei badge: ${response.code()}"
                    Log.e("UserViewModel", errorMsg)
                    _badgeState.value = BadgeState.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Exception fetching badges for user $userId", e)
                _badgeState.value = BadgeState.Error("Errore: ${e.message}")
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

    /**
     * Pulisce la cache dei badge utente.
     * Utile quando si vuole forzare un nuovo caricamento.
     */
    fun clearUserBadgeCache() {
        userBadgeCache.clear()
    }

    /**
     * Pulisce tutte le cache.
     */
    fun clearAllCache() {
        userProfileCache.clear()
        userBadgeCache.clear()
    }

    /**
     * Resetta lo stato dei badge.
     */
    fun resetBadgeState() {
        _badgeState.value = BadgeState.Idle
    }

    /**
     * Resetta lo stato del profilo utente.
     */
    fun resetUserState() {
        _userState.value = UserState.Idle
    }

    /**
     * Resetta tutti gli stati.
     */
    fun resetAllStates() {
        _userState.value = UserState.Idle
        _badgeState.value = BadgeState.Idle
    }

    /**
     * Forza un refresh dei badge dell'utente corrente.
     * Ignora la cache e fa sempre una nuova chiamata API.
     */
    fun refreshUserBadges() {
        // Pulisce la cache e ricarica
        clearUserBadgeCache()
        fetchUserBadges()
    }

    /**
     * Forza un refresh dei badge di un utente specifico.
     * Ignora la cache e fa sempre una nuova chiamata API.
     */
    fun refreshUserBadges(userId: Int) {
        // Rimuove dalla cache e ricarica
        userBadgeCache.remove(userId)
        fetchUserBadges(userId)
    }
}