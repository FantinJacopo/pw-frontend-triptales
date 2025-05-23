package com.triptales.app.viewmodel

import android.net.Uri
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

/**
 * Stati possibili per la gestione dei post
 */
sealed class PostState {
    object Idle : PostState()
    object Loading : PostState()
    data class Success(val posts: List<Post>) : PostState()
    object PostCreated : PostState()
    data class MLKitAnalyzing(val progress: String) : PostState() // Stato per analisi ML Kit
    data class Error(val message: String) : PostState()
}

/**
 * Stati possibili per la gestione dei like
 */
sealed class LikeState {
    object Idle : LikeState()
    object Loading : LikeState()
    data class Success(val likes: Map<Int, List<PostLike>>) : LikeState()
    data class UserLikesSuccess(val likedPostIds: Set<Int>, val likeMap: Map<Int, Int>) : LikeState()
    data class LikeActionSuccess(val postId: Int, val liked: Boolean, val likeId: Int? = null) : LikeState()
    data class Error(val message: String) : LikeState()
}

/**
 * ViewModel per la gestione dei post e delle interazioni dell'utente.
 */
class PostViewModel(
    private val repository: PostRepository,
    private val likeRepository: PostLikeRepository
) : ViewModel() {

    private val _postState = MutableStateFlow<PostState>(PostState.Idle)
    val postState: StateFlow<PostState> = _postState

    private val _likeState = MutableStateFlow<LikeState>(LikeState.Idle)
    val likeState: StateFlow<LikeState> = _likeState

    // Cache dei like e conteggi - ora manteniamo sia il conteggio che lo stato like
    private val userLikedPosts = mutableSetOf<Int>()
    private val userLikeIdMap = mutableMapOf<Int, Int>() // postId -> likeId

    // Ultimo gruppo caricato
    private var lastGroupId: Int? = null

    // Lista corrente dei post per accesso rapido ai dati
    private var currentPosts = listOf<Post>()

    /**
     * Carica i post per un gruppo specifico.
     */
    fun fetchPosts(groupId: Int, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            // Se è lo stesso gruppo e non è un force refresh, evita il reload
            if (!forceRefresh && _postState.value is PostState.Success && lastGroupId == groupId) {
                Log.d("PostViewModel", "Using cached posts for group $groupId")
                return@launch
            }

            lastGroupId = groupId
            _postState.value = PostState.Loading

            try {
                val response = repository.getPosts(groupId)

                if (response.isSuccessful && response.body() != null) {
                    val posts = response.body()!!
                    currentPosts = posts // Salva la lista corrente
                    Log.d("PostViewModel", "Successfully fetched ${posts.size} posts")
                    _postState.value = PostState.Success(posts)

                    // Carica i like dell'utente dopo aver caricato i post
                    fetchUserLikes()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("PostViewModel", "Error fetching posts: ${response.code()} - $errorBody")
                    _postState.value = PostState.Error("Errore nel caricamento dei post: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Exception fetching posts", e)
                _postState.value = PostState.Error("Errore: ${e.message}")
            }
        }
    }

    /**
     * Crea un nuovo post con analisi ML Kit.
     */
    fun createPost(
        groupId: Int,
        caption: String,
        imageFile: File,
        imageUri: Uri? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        viewModelScope.launch {
            // Prima mostriamo lo stato di caricamento
            _postState.value = PostState.Loading

            try {
                // Se abbiamo un URI dell'immagine, mostriamo lo stato di analisi ML Kit
                if (imageUri != null) {
                    _postState.value = PostState.MLKitAnalyzing("Analisi AI in corso...")
                }

                // Prepara il file immagine per l'upload
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

                // Creazione del post
                val response = repository.createPost(
                    groupId = groupId,
                    caption = caption,
                    imagePart = imagePart,
                    imageUri = imageUri,
                    latitude = latitude,
                    longitude = longitude
                )

                if (response.isSuccessful) {
                    Log.d("PostViewModel", "Post created successfully")
                    _postState.value = PostState.PostCreated

                    // Aggiorna lastGroupId
                    lastGroupId = groupId
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("PostViewModel", "Post creation failed: ${response.code()} - $errorBody")
                    _postState.value = PostState.Error("Errore nella creazione del post: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Exception creating post", e)
                _postState.value = PostState.Error("Errore: ${e.message}")
            }
        }
    }

    /**
     * Carica i like dell'utente corrente.
     */
    fun fetchUserLikes() {
        viewModelScope.launch {
            try {
                Log.d("PostViewModel", "Fetching user likes...")
                val response = likeRepository.getUserLikes()

                if (response.isSuccessful && response.body() != null) {
                    val likes = response.body()!!

                    // Aggiorna le cache
                    userLikedPosts.clear()
                    userLikeIdMap.clear()

                    likes.forEach { like ->
                        userLikedPosts.add(like.post)
                        userLikeIdMap[like.post] = like.id
                        Log.d("PostViewModel", "User liked post: ${like.post}")
                    }

                    _likeState.value = LikeState.UserLikesSuccess(
                        userLikedPosts.toSet(),
                        userLikeIdMap.toMap()
                    )

                    Log.d("PostViewModel", "Fetched ${likes.size} user likes: ${userLikedPosts.joinToString()}")
                } else {
                    Log.e("PostViewModel", "Error fetching user likes: ${response.code()}")
                    val errorBody = response.errorBody()?.string()
                    Log.e("PostViewModel", "Error body: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Exception fetching user likes", e)
            }
        }
    }

    /**
     * Aggiunge o rimuove un like da un post.
     */
    fun toggleLike(postId: Int) {
        viewModelScope.launch {
            _likeState.value = LikeState.Loading

            try {
                if (postId in userLikedPosts) {
                    // L'utente ha già messo like, rimuoviamolo
                    val likeId = userLikeIdMap[postId] ?: throw Exception("Like ID not found")
                    val response = likeRepository.unlikePost(likeId)

                    if (response.isSuccessful) {
                        userLikedPosts.remove(postId)
                        userLikeIdMap.remove(postId)

                        // Aggiorna il post nella lista corrente
                        updatePostLikeCount(postId, false)

                        _likeState.value = LikeState.LikeActionSuccess(postId, false)
                    } else {
                        _likeState.value = LikeState.Error("Errore rimozione like: ${response.code()}")
                    }
                } else {
                    // L'utente non ha ancora messo like, aggiungiamolo
                    val response = likeRepository.likePost(postId)

                    if (response.isSuccessful && response.body() != null) {
                        val likeResponse = response.body()!!
                        userLikedPosts.add(postId)
                        userLikeIdMap[postId] = likeResponse.id

                        // Aggiorna il post nella lista corrente
                        updatePostLikeCount(postId, true)

                        _likeState.value = LikeState.LikeActionSuccess(postId, true, likeResponse.id)
                    } else {
                        _likeState.value = LikeState.Error("Errore aggiunta like: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Exception toggling like", e)
                _likeState.value = LikeState.Error("Errore: ${e.message}")
            }
        }
    }

    /**
     * Aggiorna il conteggio like di un post nella lista corrente e aggiorna lo stato
     */
    private fun updatePostLikeCount(postId: Int, isLikeAdded: Boolean) {
        val currentState = _postState.value
        if (currentState is PostState.Success) {
            val updatedPosts = currentState.posts.map { post ->
                if (post.id == postId) {
                    val currentCount = post.likes_count ?: 0
                    val newCount = if (isLikeAdded) currentCount + 1 else maxOf(0, currentCount - 1)
                    post.copy(likes_count = newCount)
                } else {
                    post
                }
            }
            currentPosts = updatedPosts
            _postState.value = PostState.Success(updatedPosts)
        }
    }

    /**
     * Carica i like per un post specifico.
     * Ora aggiorna anche il conteggio nella lista dei post.
     */
    fun fetchPostLikes(postId: Int) {
        viewModelScope.launch {
            try {
                val response = likeRepository.getLikes(postId)

                if (response.isSuccessful && response.body() != null) {
                    val likes = response.body()!!
                    val newCount = likes.size

                    Log.d("PostViewModel", "Updated likes count for post $postId: $newCount")

                    // Aggiorna il post nella lista corrente
                    val currentState = _postState.value
                    if (currentState is PostState.Success) {
                        val updatedPosts = currentState.posts.map { post ->
                            if (post.id == postId) {
                                post.copy(likes_count = newCount)
                            } else {
                                post
                            }
                        }
                        currentPosts = updatedPosts
                        _postState.value = PostState.Success(updatedPosts)
                    }

                    // Aggiorna lo stato dei like
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
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Exception fetching post likes", e)
            }
        }
    }

    /**
     * Verifica se un post è piaciuto dall'utente.
     */
    fun isPostLiked(postId: Int): Boolean {
        val isLiked = postId in userLikedPosts
        Log.d("PostViewModel", "isPostLiked($postId) = $isLiked, userLikedPosts = ${userLikedPosts.joinToString()}")
        return isLiked
    }

    /**
     * Ottiene il conteggio dei like per un post.
     * Ora usa principalmente i dati dal modello Post.
     */
    fun getLikesCount(postId: Int): Int {
        // Prima prova a ottenere il conteggio dalla lista corrente dei post
        val post = currentPosts.find { it.id == postId }
        return post?.likes_count ?: 0
    }

    /**
     * Aggiorna il conteggio commenti per un post.
     */
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
            currentPosts = updatedPosts
            _postState.value = PostState.Success(updatedPosts)
        }
    }

    /**
     * Resetta lo stato.
     */
    fun resetState() {
        _postState.value = PostState.Idle
    }

    /**
     * Resetta completamente tutti i dati dell'utente.
     * Da chiamare quando l'utente fa logout o cambia account.
     */
    fun resetUserData() {
        Log.d("PostViewModel", "Resetting all user data...")

        // Reset degli stati
        _postState.value = PostState.Idle
        _likeState.value = LikeState.Idle

        // Pulisce le cache dei like dell'utente
        userLikedPosts.clear()
        userLikeIdMap.clear()

        // Reset della lista corrente e dell'ultimo gruppo
        currentPosts = emptyList()
        lastGroupId = null

        Log.d("PostViewModel", "User data reset completed")
    }
}