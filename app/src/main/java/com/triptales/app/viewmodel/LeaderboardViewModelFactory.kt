package com.triptales.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.triptales.app.data.leaderboard.LeaderboardRepository

class LeaderboardViewModelFactory(
    private val repository: LeaderboardRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeaderboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LeaderboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}