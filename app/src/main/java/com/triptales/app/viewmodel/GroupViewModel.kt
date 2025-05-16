package com.triptales.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.triptales.app.data.group.TripGroupRepository
import com.triptales.app.data.group.TripGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

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
                // Usa getUserGroups per ottenere solo i gruppi dell'utente
                val response = repository.getUserGroups()
                if (response.isSuccessful) {
                    val groups = response.body() ?: emptyList()
                    Log.d("GroupViewModel", "Fetched ${groups.size} user groups")
                    _groupState.value = GroupState.Success(groups)
                } else {
                    _groupState.value = GroupState.Error("Errore caricamento gruppi: ${response.code()}")
                }
            } catch (e: Exception) {
                _groupState.value = GroupState.Error("Errore: ${e.message}")
            }
        }
    }

    fun createGroup(name: String, description: String, imageFile: File) {
        viewModelScope.launch {
            _groupState.value = GroupState.Loading
            try {
                Log.d("GroupViewModel", "Creating group: $name")
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("group_image", imageFile.name, requestFile)

                val response = repository.createGroup(name, description, imagePart)
                if (response.isSuccessful && response.body() != null) {
                    val newGroup = response.body()!!
                    Log.d("GroupViewModel", "Group created successfully: ${newGroup.id}")
                    _groupState.value = GroupState.SuccessCreate(newGroup)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("GroupViewModel", "Error creating group: ${response.code()} - $errorBody")
                    _groupState.value = GroupState.Error("Errore creazione gruppo: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                Log.e("GroupViewModel", "Exception creating group", e)
                _groupState.value = GroupState.Error("Errore: ${e.message}")
            }
        }
    }

    fun resetState() {
        Log.d("GroupViewModel", "Resetting state")
        _groupState.value = GroupState.Idle
    }

    // Metodo per verificare se l'utente è creator di un gruppo
    fun isUserCreator(groupId: Int): Boolean {
        val currentState = _groupState.value
        if (currentState is GroupState.Success) {
            return currentState.groups.find { it.id == groupId }?.is_creator ?: false
        }
        return false
    }

    // Metodo per ottenere un gruppo specifico
    fun getGroup(groupId: Int): TripGroup? {
        val currentState = _groupState.value
        if (currentState is GroupState.Success) {
            return currentState.groups.find { it.id == groupId }
        }
        return null
    }
}