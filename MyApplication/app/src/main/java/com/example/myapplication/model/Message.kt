package com.example.myapplication.model

data class Message(
    val content: String,
    val isFromOpponent: Boolean, // true for opponent, false for user
    val sender: Int? = null
)

