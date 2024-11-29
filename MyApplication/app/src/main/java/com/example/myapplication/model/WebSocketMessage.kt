package com.example.myapplication.model

data class WebSocketMessage(
    val senderId: Int,
    val conversationId: Int? = null,
    val contentType: String,
    val content: String? = null
)
