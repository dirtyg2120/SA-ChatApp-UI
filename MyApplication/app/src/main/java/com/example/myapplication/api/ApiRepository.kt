package com.example.myapplication.api

import com.example.myapplication.model.FetchMessagesRequest
import com.example.myapplication.model.FetchMessagesResponse
import com.example.myapplication.model.LoginRequest
import com.example.myapplication.model.LoginResponse
import com.example.myapplication.model.UploadFileResponse
import okhttp3.MultipartBody

class ApiRepository(private val apiService: ApiService) {

    suspend fun login(username: String, password: String): LoginResponse {
        val request = LoginRequest(username, password)
        return apiService.login(request)
    }

    suspend fun fetchMessages(participantUserIds: Int, phone: String?, name: String?, cookie: String): FetchMessagesResponse {
        val request = FetchMessagesRequest(listOf(participantUserIds), phone, name)
        return apiService.fetchMessages(request, cookie)
    }

    suspend fun uploadFile(
        cookie: String,
        contentType: String,
        extension: String,
        file: MultipartBody.Part
    ): UploadFileResponse {
        val contentTypePart = MultipartBody.Part.createFormData("contentType", contentType)
        val extensionPart = MultipartBody.Part.createFormData("extension", extension)
        return apiService.uploadFile(cookie, contentTypePart, extensionPart, file)
    }
}