package com.triptales.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.triptales.app.data.group.GroupMember
import com.triptales.app.data.group.GroupMembersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class GroupMembersState {
    object Idle : GroupMembersState()
    object Loading : GroupMembersState()
    data class Success(val members: List<GroupMember>) : GroupMembersState()
    data class Error(val message: String) : GroupMembersState()
}

class GroupMembersViewModel(private val repository: GroupMembersRepository) : ViewModel() {
    private val _membersState = MutableStateFlow<GroupMembersState>(GroupMembersState.Idle)
    val membersState: StateFlow<GroupMembersState> = _membersState

    fun fetchGroupMembers(groupId: Int) {
        viewModelScope.launch {
            _membersState.value = GroupMembersState.Loading
            try {
                Log.d("GroupMembersViewModel", "Fetching members for group: $groupId")
                val response = repository.getGroupMembers(groupId)

                if (response.isSuccessful && response.body() != null) {
                    val members = response.body()!!
                    Log.d("GroupMembersViewModel", "Fetched ${members.size} members")
                    _membersState.value = GroupMembersState.Success(members)
                } else {
                    Log.e("GroupMembersViewModel", "Error fetching members: ${response.code()}")
                    _membersState.value = GroupMembersState.Error("Errore nel caricamento dei membri: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("GroupMembersViewModel", "Exception fetching members", e)
                _membersState.value = GroupMembersState.Error("Errore: ${e.message}")
            }
        }
    }

    fun resetState() {
        _membersState.value = GroupMembersState.Idle
    }
}