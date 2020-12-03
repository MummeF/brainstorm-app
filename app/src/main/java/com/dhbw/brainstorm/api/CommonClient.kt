package com.dhbw.brainstorm.api

<<<<<<< HEAD
import okhttp3.RequestBody
=======
import com.dhbw.brainstorm.api.model.Room
>>>>>>> 6a668070fb6a030de20ea2cea71e3db997fe234d
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
    fun setRoomPassword(@Body password: RequestBody, @Query("roomId") roomId: Int) : Call<Boolean>

    @POST("/api/setModeratorPassword")
<<<<<<< HEAD
    fun setModeratorPassword(@Body password: RequestBody, @Query("roomId") roomId: Int) : Call<Boolean>

    @GET("/api/hasPassword")
    fun hasPassword(@Query("roomId") roomId: Int)
=======
    fun setModeratorPassword(@Body password: String, @Query("roomId") roomId: Int) : Call<Boolean>
  
    @GET("/api/hasPassword")
    fun hasPassword(@Query("roomId") roomId: Int): Call<Boolean>

    @GET("/api/validateRoomId")
    fun validateRoomId(@Query("roomId") roomId: Int): Call<Boolean>


    @GET("/api/getRoomList")
    fun getRoomList(): Call<List<Room>>


>>>>>>> 6a668070fb6a030de20ea2cea71e3db997fe234d
}