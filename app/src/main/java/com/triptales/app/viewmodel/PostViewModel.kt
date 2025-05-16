package com.triptales.app.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.triptales.app.data.post.Post
import com.triptales.app.data.post.PostRepository
import com.triptales.app.data.utils.uriToFile
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

class PostViewModel(private val repository: PostRepository) : ViewModel() {
    private val _postState = MutableStateFlow<PostState>(PostState.Idle)
    val postState: StateFlow<PostState> = _postState

    // Mantieni traccia dell'ultimo gruppo caricato per il refresh automatico
    private var lastGroupId: Int? = null

    fun fetchPosts(groupId: Int, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            Log.d("PostViewModel", "Fetching posts for group: $groupId (force: $forceRefresh)")

            // Se non è un force refresh e stiamo già mostrando i post di questo gruppo, non ricaricare
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

    fun createPost(groupId: Int, caption: String, imageFile: File) {
        viewModelScope.launch {
            Log.d("PostViewModel", "Creating post for group: $groupId, caption: $caption")
            _postState.value = PostState.Loading
            try {
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

                val response = repository.createPost(groupId, caption, imagePart)
                Log.d("PostViewModel", "Create post response code: ${response.code()}")

                if (response.isSuccessful) {
                    Log.d("PostViewModel", "Post created successfully")
                    _postState.value = PostState.PostCreated
                    // Ricarica automaticamente i post del gruppo
                    fetchPosts(groupId, forceRefresh = true)
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

    // Metodo per refreshare i post quando un commento viene aggiunto
    fun refreshPosts() {
        lastGroupId?.let { groupId ->
            Log.d("PostViewModel", "Refreshing posts for group $groupId")
            fetchPosts(groupId, forceRefresh = true)
        }
    }

    fun resetState() {
        Log.d("PostViewModel", "Resetting state")
        _postState.value = PostState.Idle
    }
}