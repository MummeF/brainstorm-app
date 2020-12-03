package com.dhbw.brainstorm.api.model

data class Contribution(
    val comments: List<Comment>,
    val content: String,
    val id: Int,
    val reputation: Int,
    val subject: String
)