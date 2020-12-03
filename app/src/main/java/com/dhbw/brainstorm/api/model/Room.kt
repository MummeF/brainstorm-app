package com.dhbw.brainstorm.api.model

data class Room(
    val contributions: List<Contribution>,
    val description: String,
    val id: Int,
    val moderatorId: String,
    val `public`: Boolean,
    val state: RoomState,
    val topic: String
)