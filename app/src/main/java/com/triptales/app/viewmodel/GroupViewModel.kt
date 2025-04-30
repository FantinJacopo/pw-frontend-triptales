package com.triptales.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.triptales.app.data.CreateGroupRequest
import com.triptales.app.data.TripGroupRepository
import com.triptales.app.model.TripGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class GroupState {
    object Idle : GroupState()
    object Loading : GroupState()
    data class Success(val groups: List<TripGroup>) : GroupState()
    data class SuccessCreate(val newGroup: TripGroup) : GroupState()
    data class Error(val message: String) : GroupState()
}

class GroupViewModel(private val repository: TripGroupRepository) : ViewModel() {

    private val _groupState = MutableStateFlow<GroupState>(GroupState.Idle)
    val groupState: StateFlow<GroupState> = _groupState

    fun fetchGroups() {
        viewModelScope.launch {
            _groupState.value = GroupState.Loading
            try {
                val response = repository.getGroups()
                if (response.isSuccessful) {
                    _groupState.value = GroupState.Success(response.body() ?: emptyList())
                } else {
                    _groupState.value = GroupState.Error("Errore caricamento gruppi: ${response.code()}")
                }
            } catch (e: Exception) {
                _groupState.value = GroupState.Error("Errore: ${e.message}")
            }
        }
    }

    fun createGroup(name: String, imageUrl: String, description: String) {
        viewModelScope.launch {
            _groupState.value = GroupState.Loading
            try {
                val request = CreateGroupRequest(name, imageUrl, description)
                val response = repository.createGroup(request)
                if (response.isSuccessful && response.body() != null) {
                    val newGroup = response.body()!!
                    _groupState.value = GroupState.SuccessCreate(newGroup)
                    fetchGroups() // opzionale: aggiorna la lista dopo creazione
                } else {
                    _groupState.value = GroupState.Error("Errore creazione gruppo: ${response.code()}")
                }
            } catch (e: Exception) {
                _groupState.value = GroupState.Error("Errore: ${e.message}")
            }
        }
    }

    fun resetState() {
        _groupState.value = GroupState.Idle
    }
}
