package com.dhbw.brainstorm.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface RoomClient {
    @POST("/api/setPassword")
    fun setRoomPassword(@Body password: Int, @Query("roomId") roomId: Int) : Call<Boolean>

    @POST("/api/setModeratorPassword")
    fun setModeratorPassword(@Body password: Int, @Query("roomId") roomId: Int) : Call<Boolean>
}