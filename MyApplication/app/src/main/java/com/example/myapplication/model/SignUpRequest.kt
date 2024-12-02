package com.example.myapplication.model

data class SignUpRequest(
    val name: String,
    val phone: String,
    val password: String,
    val dateOfBrith: String,
    val email: String
)