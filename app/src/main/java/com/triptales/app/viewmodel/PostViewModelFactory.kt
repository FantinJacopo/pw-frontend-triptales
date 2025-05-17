package com.triptales.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.triptales.app.data.post.PostLikeRepository
import com.triptales.app.data.post.PostRepository

class PostViewModelFactory(
    private val repository: PostRepository,
    private val likeRepository: PostLikeRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
            return PostViewModel(repository, likeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}