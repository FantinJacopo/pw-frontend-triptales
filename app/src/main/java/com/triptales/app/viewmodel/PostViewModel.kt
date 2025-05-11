package com.triptales.app.viewmodel

import android.content.Context
import android.net.Uri
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

    fun createPost(groupId: Int, caption: String, imageFile: File) {
        viewModelScope.launch {
            _postState.value = PostState.Loading
            try {
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

                val response = repository.createPost(groupId, caption, imagePart)
                if (response.isSuccessful) {
                    fetchPosts(groupId)
                } else {
                    _postState.value = PostState.Error("Errore creazione post")
                }
            } catch (e: Exception) {
                _postState.value = PostState.Error("Errore: ${e.message}")
            }
        }
    }

    fun createPostConImmagine(
        uri: Uri,
        groupId: Int,
        caption: String,
        context: Context
    ) {
        viewModelScope.launch {
            val file = uriToFile(uri, context)
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("image", file.name, requestBody)

            try {
                val response = repository.createPost(
                    tripGroupId = groupId,
                    smartCaption = caption,
                    imagePart = multipartBody
                )
                // TODO: Gestisci il successo
            } catch (e: Exception) {
                // TODO: Gestisci l'errore
            }
        }
    }

}
