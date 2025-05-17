package com.triptales.app.data.post

import com.triptales.app.data.utils.ApiUtils.safeApiCall

class PostLikeRepository(private val api: PostLikeApi) {
    suspend fun likePost(postId: Int) = safeApiCall(
        tag = "PostLikeRepository",
        operation = "like post",
        apiCall = { api.likePost(PostLikeRequest(post = postId)) }
    )

    suspend fun unlikePost(likeId: Int) = safeApiCall(
        tag = "PostLikeRepository",
        operation = "unlike post",
        apiCall = { api.unlikePost(likeId) }
    )

    suspend fun getLikes(postId: Int) = safeApiCall(
        tag = "PostLikeRepository",
        operation = "get post likes",
        apiCall = { api.getLikes(postId) }
    )

    suspend fun getUserLikes() = safeApiCall(
        tag = "PostLikeRepository",
        operation = "get user likes",
        apiCall = { api.getUserLikes() }
    )
}