package com.dhbw.brainstorm.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query



interface CommonClient {

    @GET("/api/isAlive")
    fun isAlive(): Call<String>


    //Creates room and returns id
    @GET("/api/createRoom")
    fun createRoom(
        @Query("description") description: String,
        @Query("isPublic") isPublic: Boolean,
        @Query("moderatorId") moderatorId: String,
        @Query("topic") topic: String
    ): Call<Int>

    @POST("/api/setPassword")
    fun setRoomPassword(@Body password: Int, @Query("roomId") roomId: Int) : Call<Boolean>

    @POST("/api/setModeratorPassword")
    fun setModeratorPassword(@Body password: Int, @Query("roomId") roomId: Int) : Call<Boolean>

    @GET("/api/hasPassword")
    fun hasPassword(@Query("roomId") roomId: Int)

}