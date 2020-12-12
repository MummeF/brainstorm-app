package com.dhbw.brainstorm.api

import com.dhbw.brainstorm.api.model.Contribution
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ContributionClient {


    @POST("/api/addContribution")
    fun addContribution(@Query("roomId") roomId: Int, @Body content: RequestBody): Call<String>

    @DELETE("/api/deleteContribution")
    fun deleteContribution(
        @Query("roomId") roomId: Int,
        @Query("contributionId") contributionId: Int
    ): Call<String>

    @GET("/api/addContributionSubject")
    fun addContributionSubject(
        @Query("roomId") roomId: Int,
        @Query("contributionId") contributionId: Int,
        @Query("subject") subject: String
    ): Call<String>

    @PUT("/api/updateContribution")
    fun updateContribution(
        @Query("roomId") roomId: Int,
        @Query("contributionId") contributionId: Int,
        @Query("subject") subject: String,
        @Query("content") content: String
    ): Call<String>


    @GET("/api/voteContributionUp")
    fun voteContributionUp(
        @Query("roomId") roomId: Int,
        @Query("contributionId") contributionId: Int
    ): Call<String>

    @GET("/api/voteContributionDown")
    fun voteContributionDown(
        @Query("roomId") roomId: Int,
        @Query("contributionId") contributionId: Int
    ): Call<String>

    @GET("/api/getContribution")
    fun getContribution(
        @Query("roomId") roomId: Int,
        @Query("contributionId") contributionId: Int
    ): Call<Contribution>
}