package com.dhbw.brainstorm.api

import com.dhbw.brainstorm.api.model.Room
import com.dhbw.brainstorm.api.model.RoomState
import retrofit2.Call
import retrofit2.http.*

interface RoomClient {
    @POST("/api/setPassword")
    fun setRoomPassword(@Body password: Int, @Query("roomId") roomId: Int): Call<Boolean>

    @POST("/api/setModeratorPassword")
    fun setModeratorPassword(@Body password: Int, @Query("roomId") roomId: Int): Call<Boolean>

    @POST("/api/setRoomState")
    fun setRoomState(@Body state: RoomState, @Query("roomId") roomId: Int): Call<Boolean>

    @PUT("/api/updateRoom")
    fun updateRoom(@Body state: Room): Call<Boolean>

    @DELETE("/api/deleteRoom")
    fun deleteRoom(
        @Query("roomId") roomId: Int
    ): Call<String>

    @GET("/api/getRoom")
    fun getRoom(
        @Query("roomId") roomId: Int
    ): Call<Room>

    @GET("/api/getHistoryRoom")
    fun getHistoryRoom(
        @Query("roomId") roomId: Int
    ): Call<Room>

    @GET("/api/increaseRoomState")
    fun increaseRoomState(
        @Query("roomId") roomId: Int
    ): Call<String>

    @POST("/api/validatePassword")
    fun validatePassword(@Query("roomId") roomId: Int, @Body password: String): Call<Boolean>

    @POST("/api/validateModeratorPassword")
    fun validateModeratorPassword(
        @Query("roomId") roomId: Int,
        @Body moderatorPassword: String
    ): Call<Boolean>

    @GET("/api/validateModeratorId")
    fun validateModeratorId(
        @Query("roomId") roomId: Int,
        @Query("moderatorId") moderatorId: String
    ): Call<Boolean>


    @GET("/api/setModeratorId")
    fun setModeratorId(
        @Query("roomId") roomId: Int,
        @Query("moderatorId") moderatorId: String
    ): Call<String>


}