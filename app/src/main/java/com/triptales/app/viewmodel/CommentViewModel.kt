package com.triptales.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.triptales.app.data.comment.Comment
import com.triptales.app.data.comment.CommentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CommentState {
    object Idle : CommentState()
    object Loading : CommentState()
    data class Success(val comments: List<Comment>) : CommentState()
    data class Error(val message: String) : CommentState()
    data class CommentCreated(val postId: Int, val newCommentCount: Int) : CommentState()
}

class CommentViewModel(private val repository: CommentRepository) : ViewModel() {
    private val _commentState = MutableStateFlow<CommentState>(CommentState.Idle)
    val commentState: StateFlow<CommentState> = _commentState

    fun fetchComments(postId: Int) {
        viewModelScope.launch {
            _commentState.value = CommentState.Loading
            try {
                val response = repository.getCommentsByPost(postId)
                if (response.isSuccessful && response.body() != null) {
                    _commentState.value = CommentState.Success(response.body()!!)
                } else {
                    _commentState.value = CommentState.Error("Errore nel caricamento commenti: ${response.code()}")
                }
            } catch (e: Exception) {
                _commentState.value = CommentState.Error("Errore: ${e.message}")
            }
        }
    }

    fun createComment(postId: Int, content: String) {
        viewModelScope.launch {
            try {
                val response = repository.createComment(postId, content)
                if (response.isSuccessful) {
                    // Ricarica i commenti per ottenere la lista aggiornata
                    val commentsResponse = repository.getCommentsByPost(postId)
                    if (commentsResponse.isSuccessful && commentsResponse.body() != null) {
                        val comments = commentsResponse.body()!!
                        _commentState.value = CommentState.Success(comments)
                        // Notifica il nuovo conteggio commenti
                        _commentState.value = CommentState.CommentCreated(postId, comments.size)
                    } else {
                        fetchComments(postId) // Fallback
                    }
                } else {
                    _commentState.value = CommentState.Error("Errore nella creazione del commento: ${response.code()}")
                }
            } catch (e: Exception) {
                _commentState.value = CommentState.Error("Errore: ${e.message}")
            }
        }
    }

    fun resetState() {
        _commentState.value = CommentState.Idle
    }
}