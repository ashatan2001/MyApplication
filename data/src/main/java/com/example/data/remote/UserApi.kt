package com.example.data.remote

import com.example.data.dto.UserDto
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApi {
    @GET("{id}")
    suspend fun getUser(@Path("id") id: Int): UserDto

    /*@POST("posts")
    suspend fun createPost(
        @Body post: CreatePostRequest
    ): Response<PostResponse>

    @GET("users")
    suspend fun getUserWithHeaders(
        @Header("Authorization") token: String
    ): Response<UserDto>
    */

}