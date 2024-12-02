package com.example.myapplication.api

import com.example.myapplication.model.FetchMessagesRequest
import com.example.myapplication.model.FetchMessagesResponse
import com.example.myapplication.model.FileUploadResponse
import com.example.myapplication.model.LoginRequest
import com.example.myapplication.model.LoginResponse
import com.example.myapplication.model.SignUpRequest
import com.example.myapplication.model.SignUpResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ApiRepository(private val apiService: ApiService) {

    suspend fun login(username: String, password: String): LoginResponse {
        val request = LoginRequest(username, password)
        return apiService.login(request)
    }

    suspend fun fetchMessages(participantUserIds: Int, phone: String?, name: String?, cookie: String): FetchMessagesResponse {
        val request = FetchMessagesRequest(listOf(participantUserIds), phone, name)
        return apiService.fetchMessages(request, cookie)
    }

    suspend fun uploadFile(cookie: String, contentType: RequestBody, extension: RequestBody, file: MultipartBody.Part): FileUploadResponse {
        return apiService.uploadFile(cookie, contentType, extension, file)
    }

    // Add signUp method to call the signUp API
    suspend fun signUp(cookie: String, name: String, phone: String, password: String, dateOfBrith: String, email: String): SignUpResponse {
        val request = SignUpRequest(name, phone, password, dateOfBrith, email)
        return apiService.signUp(cookie, request)
    }
}
