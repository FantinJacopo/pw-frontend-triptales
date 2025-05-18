package com.triptales.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.triptales.app.data.post.Post
import com.triptales.app.data.post.PostLike
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

sealed class LikeState {
    object Idle : LikeState()
    object Loading : LikeState()
    data class Success(val likes: Map<Int, List<PostLike>>) : LikeState()
    data class UserLikesSuccess(val likedPostIds: Set<Int>, val likeMap: Map<Int, Int>) : LikeState()
    data class LikeActionSuccess(val postId: Int, val liked: Boolean, val likeId: Int? = null) : LikeState()
    data class Error(val message: String) : LikeState()
}

class PostViewModel(
    private val repository: PostRepository,
    private val likeRepository: PostLikeRepository
) : ViewModel() {
    private val _postState = MutableStateFlow<PostState>(PostState.Idle)
    val postState: StateFlow<PostState> = _postState

    private val _likeState = MutableStateFlow<LikeState>(LikeState.Idle)
    val likeState: StateFlow<LikeState> = _likeState

    private val likesCountMap = mutableMapOf<Int, Int>()

    // Cache delle informazioni sui like
    private val userLikedPosts = mutableSetOf<Int>()
    private val userLikeIdMap = mutableMapOf<Int, Int>() // postId -> likeId

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
                    _postState.value = PostState.Success(posts)

                    // Carica i like dell'utente dopo aver caricato i post
                    fetchUserLikes()
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

    // Carica i like dell'utente
    fun fetchUserLikes() {
        viewModelScope.launch {
            try {
                Log.d("PostViewModel", "Fetching user likes")
                val response = likeRepository.getUserLikes()
                if (response.isSuccessful && response.body() != null) {
                    val likes = response.body()!!

                    // Aggiorna le cache
                    userLikedPosts.clear()
                    userLikeIdMap.clear()

                    likes.forEach { like ->
                        userLikedPosts.add(like.post)
                        userLikeIdMap[like.post] = like.id
                    }

                    _likeState.value = LikeState.UserLikesSuccess(
                        userLikedPosts.toSet(),
                        userLikeIdMap.toMap()
                    )

                    Log.d("PostViewModel", "Fetched ${likes.size} user likes")
                } else {
                    Log.e("PostViewModel", "Error fetching user likes: ${response.code()}")
                    _likeState.value = LikeState.Error("Errore nel caricamento dei mi piace")
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Exception fetching user likes", e)
                _likeState.value = LikeState.Error("Errore: ${e.message}")
            }
        }
    }

    // Gestisce il toggle del like
    fun toggleLike(postId: Int) {
        viewModelScope.launch {
            _likeState.value = LikeState.Loading

            try {
                if (postId in userLikedPosts) {
                    // L'utente ha già messo like, quindi rimuoviamolo
                    val likeId = userLikeIdMap[postId] ?: throw Exception("Like ID not found")
                    val response = likeRepository.unlikePost(likeId)

                    if (response.isSuccessful) {
                        userLikedPosts.remove(postId)
                        userLikeIdMap.remove(postId)
                        _likeState.value = LikeState.LikeActionSuccess(postId, false)
                        Log.d("PostViewModel", "Post $postId unliked successfully")
                    } else {
                        _likeState.value = LikeState.Error("Errore nella rimozione del mi piace")
                    }
                } else {
                    // L'utente non ha ancora messo like, aggiungiamolo
                    val response = likeRepository.likePost(postId)

                    if (response.isSuccessful && response.body() != null) {
                        val likeResponse = response.body()!!
                        userLikedPosts.add(postId)
                        userLikeIdMap[postId] = likeResponse.id
                        _likeState.value = LikeState.LikeActionSuccess(postId, true, likeResponse.id)
                        Log.d("PostViewModel", "Post $postId liked successfully, like ID: ${likeResponse.id}")
                    } else {
                        _likeState.value = LikeState.Error("Errore nell'aggiunta del mi piace")
                    }
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Exception toggling like", e)
                _likeState.value = LikeState.Error("Errore: ${e.message}")
            }
        }
    }

    fun isPostLiked(postId: Int): Boolean {
        return postId in userLikedPosts
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

    // Metodo per incrementare il conteggio commenti di un singolo post
    fun incrementPostCommentCount(postId: Int) {
        val currentState = _postState.value
        if (currentState is PostState.Success) {
            val updatedPosts = currentState.posts.map { post ->
                if (post.id == postId) {
                    val newCount = (post.comments_count ?: 0) + 1
                    Log.d("PostViewModel", "Incrementing comment count for post $postId to $newCount")
                    post.copy(comments_count = newCount)
                } else {
                    post
                }
            }
            _postState.value = PostState.Success(updatedPosts)
        }
    }

    // Metodo per refreshare i post quando un commento viene aggiunto
    fun refreshPosts() {
        lastGroupId?.let { groupId ->
            Log.d("PostViewModel", "Refreshing posts for group $groupId")
            fetchPosts(groupId, forceRefresh = true)
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

    // Funzione per ottenere i like di un post specifico
    fun fetchPostLikes(postId: Int) {
        viewModelScope.launch {
            try {
                val response = likeRepository.getLikes(postId)
                if (response.isSuccessful && response.body() != null) {
                    val likes = response.body()!!
                    likesCountMap[postId] = likes.size

                    // Aggiorna lo stato
                    val currentLikeState = _likeState.value
                    if (currentLikeState is LikeState.Success) {
                        val updatedLikes = currentLikeState.likes.toMutableMap()
                        updatedLikes[postId] = likes
                        _likeState.value = LikeState.Success(updatedLikes)
                    } else {
                        val newLikes = mutableMapOf<Int, List<PostLike>>()
                        newLikes[postId] = likes
                        _likeState.value = LikeState.Success(newLikes)
                    }

                    Log.d("PostViewModel", "Fetched ${likes.size} likes for post $postId")
                } else {
                    Log.e("PostViewModel", "Error fetching post likes: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Exception fetching post likes", e)
            }
        }
    }

    // Funzione per ottenere il conteggio di like di un post
    fun getLikesCount(postId: Int): Int {
        return likesCountMap[postId] ?: 0
    }
}