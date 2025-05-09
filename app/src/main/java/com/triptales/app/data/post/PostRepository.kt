package com.triptales.app.data.post

import com.triptales.app.data.PostApi

class PostRepository(private val api: PostApi) {
    suspend fun getPosts(groupId: Int) = api.getPosts(groupId)
}