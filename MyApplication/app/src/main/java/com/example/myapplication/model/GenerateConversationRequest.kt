package com.example.myapplication.model

data class GenerateConversationRequest(
    val Admin: Int,
    val participants: List<Int>,
    val name: String
)