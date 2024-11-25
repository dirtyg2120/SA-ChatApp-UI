package com.example.myapplication.api

import com.example.myapplication.model.FetchMessagesRequest
import com.example.myapplication.model.FetchMessagesResponse
import com.example.myapplication.model.LoginRequest
import com.example.myapplication.model.LoginResponse

class ApiRepository(private val apiService: ApiService) {

    suspend fun login(username: String, password: String): LoginResponse {
        val request = LoginRequest(username, password)
        return apiService.login(request)
    }

    suspend fun fetchMessages(conversationId: Int, page: Int, pageSize: Int, cookie: String): FetchMessagesResponse {
        val request = FetchMessagesRequest(conversationId, page, pageSize)
        return apiService.fetchMessages(request, cookie)
    }
}