package com.dhbw.brainstorm.api.model

data class Room(
    val contributions: ArrayList<Contribution>,
    val description: String,
    val id: Int,
    var moderatorId: String,
    val `public`: Boolean,
    var state: RoomState,
    val topic: String
)