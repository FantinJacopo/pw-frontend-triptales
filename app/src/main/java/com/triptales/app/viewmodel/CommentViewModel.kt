package com.triptales.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.triptales.app.data.comment.Comment
import com.triptales.app.data.comment.CommentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    // Teniamo traccia del job corrente per cancellare operazioni in corso se necessario
    private var currentJob: Job? = null

    fun fetchComments(postId: Int) {
        // Cancella eventuali job in corso
        currentJob?.cancel()

        currentJob = viewModelScope.launch {
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
        // Cancella eventuali job in corso
        currentJob?.cancel()

        currentJob = viewModelScope.launch {
            try {
                // Impostiamo lo stato di loading per mostrare feedback all'utente
                _commentState.value = CommentState.Loading

                val response = repository.createComment(postId, content)
                if (response.isSuccessful) {
                    // Ricarica i commenti per ottenere la lista aggiornata
                    val commentsResponse = repository.getCommentsByPost(postId)
                    if (commentsResponse.isSuccessful && commentsResponse.body() != null) {
                        val comments = commentsResponse.body()!!

                        // Emetti solo l'evento CommentCreated (che mostrerà brevemente il loading)
                        _commentState.value = CommentState.CommentCreated(postId, comments.size)

                        // Dopo un breve ritardo, torna allo stato Success con i dati aggiornati
                        delay(500) // Breve ritardo per permettere all'UI di reagire
                        _commentState.value = CommentState.Success(comments)
                    } else {
                        _commentState.value = CommentState.Error("Errore nel caricamento commenti aggiornati")
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
        currentJob?.cancel()
        _commentState.value = CommentState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }
}