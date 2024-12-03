package com.example.myapplication.model

data class FindUserRequest(
    val phone: String? = null,
    val name: String? = null
)

data class FindUserResponse(
    val content: List<User>
)

data class User(
    val id: Int,
    val UserName: String,
    val phone: String,
    val profilePhoto: String? = null,
    val friendStatus: String
)