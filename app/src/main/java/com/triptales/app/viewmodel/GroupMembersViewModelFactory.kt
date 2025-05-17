package com.triptales.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.triptales.app.data.group.GroupMembersRepository

class GroupMembersViewModelFactory(
    private val repository: GroupMembersRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupMembersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroupMembersViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}