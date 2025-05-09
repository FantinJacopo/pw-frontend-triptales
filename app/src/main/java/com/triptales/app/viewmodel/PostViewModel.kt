package com.triptales.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.triptales.app.data.post.PostRepository
import com.triptales.app.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PostState {
    object Idle : PostState()
    object Loading : PostState()
    data class Success(val posts: List<Post>) : PostState()
    data class Error(val message: String) : PostState()
}

class PostViewModel(private val repository: PostRepository) : ViewModel() {
    private val _postState = MutableStateFlow<PostState>(PostState.Idle)
    val postState: StateFlow<PostState> = _postState

    fun fetchPosts(groupId: Int) {
        viewModelScope.launch {
            _postState.value = PostState.Loading
            try {
                val response = repository.getPosts(groupId)
                if (response.isSuccessful && response.body() != null) {
                    val posts = response.body()!!
                    _postState.value = PostState.Success(posts)
                } else {
                    val errorBody = response.errorBody()?.string()
                    _postState.value = PostState.Error("Errore caricamento post: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                _postState.value = PostState.Error("Errore: ${e.message}")
            }
        }
    }

}
