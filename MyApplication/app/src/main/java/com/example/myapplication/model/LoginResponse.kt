package com.example.myapplication.model


data class LoginResponse(
    val accessToken: String,
    val tokenType: String,
    val userId: Int
)
