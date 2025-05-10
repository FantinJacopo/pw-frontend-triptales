package com.triptales.app.data.post

import com.triptales.app.data.post.PostApi

class PostRepository(private val api: PostApi) {
    suspend fun getPosts(groupId: Int) = api.getPosts(groupId)
    suspend fun createPost(request: CreatePostRequest) = api.createPost(request)
}