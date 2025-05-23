package com.triptales.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.triptales.app.data.leaderboard.GroupLeaderboard
import com.triptales.app.data.leaderboard.LeaderboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LeaderboardState {
    object Idle : LeaderboardState()
    object Loading : LeaderboardState()
    data class Success(val leaderboard: GroupLeaderboard) : LeaderboardState()
    data class Error(val message: String) : LeaderboardState()
}

class LeaderboardViewModel(private val repository: LeaderboardRepository) : ViewModel() {

    private val _leaderboardState = MutableStateFlow<LeaderboardState>(LeaderboardState.Idle)
    val leaderboardState: StateFlow<LeaderboardState> = _leaderboardState

    fun fetchGroupLeaderboard(groupId: Int) {
        viewModelScope.launch {
            _leaderboardState.value = LeaderboardState.Loading

            try {
                Log.d("LeaderboardViewModel", "Fetching leaderboard for group: $groupId")
                val response = repository.getGroupLeaderboard(groupId)

                if (response.isSuccessful && response.body() != null) {
                    val leaderboard = response.body()!!
                    Log.d("LeaderboardViewModel", "Leaderboard loaded: ${leaderboard.leaderboard.size} users")
                    _leaderboardState.value = LeaderboardState.Success(leaderboard)
                } else {
                    val errorMsg = "Errore nel caricamento della classifica: ${response.code()}"
                    Log.e("LeaderboardViewModel", errorMsg)
                    _leaderboardState.value = LeaderboardState.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("LeaderboardViewModel", "Exception fetching leaderboard", e)
                _leaderboardState.value = LeaderboardState.Error("Errore: ${e.message}")
            }
        }
    }

    fun resetState() {
        _leaderboardState.value = LeaderboardState.Idle
    }
}
