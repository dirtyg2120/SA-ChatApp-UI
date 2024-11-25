package com.example.myapplication.model

data class Message(
    val id: Int? = null,
    val admin: Int? = null,
    val chatMessages: List<ChatMessage>? = emptyList(),
    val participants: List<Participant>? = emptyList(),
    val lastMessageTime: String? = null,
    val creationTime: String? = null,
    val privateChat: Boolean? = true,
    val status: String? = "ACTIVE",

    val content: String? = "sth",
    val isFromOpponent: Boolean, // true for opponent, false for user
    val sender: Int? = null,
    val conversationId: Int? = null,
)

data class ChatMessage(
    val content: String? = "",
    val contentType: String? = "TEXT",
    val creationTime: Long? = null,
    val conversationId: Int? = null,
    val sender: Int? = null,
    val id: Int? = null,
)

data class Participant(
    val conversationDisplayName: String? = "",
    val userId: Int? = null,
    val conversationId: Int? = null,
    val participantName: String? = null,
    val lastView: Long? = null,
    val profilePhoto: String? = null,
)