package com.example.myapplication.model

data class GenerateConversationResponse(
    val success: Boolean,
    val message: String,
    val conversationId: Int
)