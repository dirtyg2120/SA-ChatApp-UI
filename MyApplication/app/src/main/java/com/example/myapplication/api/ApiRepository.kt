package com.example.myapplication.api

import com.example.myapplication.model.ChatRoom
import com.example.myapplication.model.LoginRequest
import com.example.myapplication.model.LoginResponse

class ApiRepository(private val apiService: ApiService) {

    suspend fun login(username: String, password: String): LoginResponse {
        val request = LoginRequest(username, password)
        return apiService.login(request)
    }

    suspend fun getRoomList(): List<ChatRoom> {
        return apiService.getRoomList()
    }
}