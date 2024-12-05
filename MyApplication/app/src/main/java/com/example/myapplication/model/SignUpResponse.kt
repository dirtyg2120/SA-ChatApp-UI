package com.example.myapplication.model

data class SignUpResponse(
    val success: Boolean,
    val id: Int,
    val error: Boolean? = false,
    val message: String? = null
)
