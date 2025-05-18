package com.triptales.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.triptales.app.data.post.Post
import com.triptales.app.data.post.PostLikeRepository
import com.triptales.app.data.post.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

sealed class PostState {
    object Idle : PostState()
    object Loading : PostState()
    data class Success(val posts: List<Post>) : PostState()
    object PostCreated : PostState()
    data class Error(val message: String) : PostState()
}

class PostViewModel(
    private val repository: PostRepository,
    private val likeRepository: PostLikeRepository
) : ViewModel() {
    private val _postState = MutableStateFlow<PostState>(PostState.Idle)
    val postState: StateFlow<PostState> = _postState

    // Mantieni traccia dell'ultimo gruppo caricato per il refresh automatico
    private var lastGroupId: Int? = null

    fun fetchPosts(groupId: Int, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            Log.d("PostViewModel", "Fetching posts for group: $groupId (force: $forceRefresh)")

            // Se è lo stesso gruppo e non è un force refresh, evita il reload
            if (!forceRefresh && _postState.value is PostState.Success && lastGroupId == groupId) {
                Log.d("PostViewModel", "Already showing posts for group $groupId, skipping")
                return@launch
            }

            lastGroupId = groupId
            _postState.value = PostState.Loading

            try {
                val response = repository.getPosts(groupId)
                Log.d("PostViewModel", "Response code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val posts = response.body()!!
                    Log.d("PostViewModel", "Received ${posts.size} posts")
                    posts.forEach { post ->
                        Log.d("PostViewModel", "Post ${post.id}: ${post.smart_caption}")
                        Log.d("PostViewModel", "  - comments_count: ${post.comments_count}")
                        Log.d("PostViewModel", "  - location: ${post.latitude}, ${post.longitude}")
                    }
                    _postState.value = PostState.Success(posts)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("PostViewModel", "Error response: $errorBody")
                    _postState.value = PostState.Error("Errore caricamento post: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Exception fetching posts", e)
                _postState.value = PostState.Error("Errore: ${e.message}")
            }
        }
    }

    fun createPost(
        groupId: Int,
        caption: String,
        imageFile: File,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        viewModelScope.launch {
            Log.d("PostViewModel", "Creating post for group: $groupId, caption: $caption")
            Log.d("PostViewModel", "Location: lat=$latitude, lng=$longitude")
            _postState.value = PostState.Loading
            try {
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

                val response = repository.createPost(
                    groupId = groupId,
                    caption = caption,
                    imagePart = imagePart,
                    latitude = latitude,
                    longitude = longitude
                )
                Log.d("PostViewModel", "Create post response code: ${response.code()}")

                if (response.isSuccessful) {
                    Log.d("PostViewModel", "Post created successfully")
                    // Importante: Prima impostiamo lo stato a PostCreated
                    _postState.value = PostState.PostCreated

                    // Poi aggiorniamo lastGroupId per assicurarci che quando torneremo
                    // alla schermata del gruppo, i post saranno aggiornati
                    lastGroupId = groupId
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("PostViewModel", "Error creating post: $errorBody")
                    _postState.value = PostState.Error("Errore creazione post: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Exception creating post", e)
                _postState.value = PostState.Error("Errore: ${e.message}")
            }
        }
    }

    // Metodo per aggiornare il conteggio commenti di un singolo post
    fun updatePostCommentCount(postId: Int, newCount: Int) {
        val currentState = _postState.value
        if (currentState is PostState.Success) {
            val updatedPosts = currentState.posts.map { post ->
                if (post.id == postId) {
                    post.copy(comments_count = newCount)
                } else {
                    post
                }
            }
            _postState.value = PostState.Success(updatedPosts)
            Log.d("PostViewModel", "Updated comment count for post $postId to $newCount")
        }
    }

    // Metodo per ottenere tutti i post con posizione per la mappa
    fun getPostsWithLocation(): List<Post> {
        val currentState = _postState.value
        return if (currentState is PostState.Success) {
            currentState.posts.filter { post ->
                post.latitude != null && post.longitude != null
            }
        } else {
            emptyList()
        }
    }

    fun resetState() {
        Log.d("PostViewModel", "Resetting state")
        _postState.value = PostState.Idle
    }
}