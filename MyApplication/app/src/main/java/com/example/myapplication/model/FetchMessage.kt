package com.example.myapplication.model

data class FetchMessagesRequest(
    val conversationId: Int,
    val page: Int,
    val pageSize: Int
)

data class FetchMessagesResponse(
    val content: List<Message>,
    val pageable: Pageable,
    val totalElements: Int,
    val totalPages: Int,
    val last: Boolean,
    val first: Boolean
)

//data class Message(
//    val content: String,
//    val contentType: String,
//    val creationTime: Long,
//    val conversationId: Int,
//    val sender: Int,
//    val id: Int
//)

data class Pageable(
    val pageNumber: Int,
    val pageSize: Int,
    val offset: Int,
    val paged: Boolean,
    val unpaged: Boolean
)
