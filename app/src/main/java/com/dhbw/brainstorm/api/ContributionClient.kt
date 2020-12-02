package com.dhbw.brainstorm.api

import retrofit2.Call
import retrofit2.http.*

interface ContributionClient {



    @POST("/api/addContribution")
    fun addContribution(@Query("roomId") roomId: Int, @Body content: String): Call<String>

    @DELETE("/api/deleteContribution")
    fun deleteContribution(
        @Query("roomId") roomId: Int,
        @Query("contributionId") contributionId: Int
    )

    @GET("/api/addContributionSubject")
    fun addContributionSubject(
        @Query("roomId") roomId: Int,
        @Query("contributionId") contributionId: Int,
        @Query("subject") subject: String
    )

    @GET("/api/updateContribution")
    fun updateContribution(
        @Query("roomId") roomId: Int,
        @Query("contributionId") contributionId: Int,
        @Query("subject") subject: String
    )

}