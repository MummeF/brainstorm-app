package com.dhbw.brainstorm.api

import retrofit2.Call
import retrofit2.http.*

interface CommentClient {
    @POST("/api/addComment")
    fun addComment(@Query("roomId") roomId: Int, @Body content: String): Call<String>

    @GET("/api/voteCommentUp")
    fun voteCommentUp(
        @Query("roomId") roomId: Int,
        @Query("contributionId") contributionId: Int,
        @Query("commentId") commentId: Int
    )
    @GET("/api/voteCommentDown")
    fun voteCommentDown(
        @Query("roomId") roomId: Int,
        @Query("contributionId") contributionId: Int,
        @Query("commentId") commentId: Int
    )
}