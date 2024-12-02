package com.example.myapplication.api

import com.example.myapplication.model.FetchMessagesRequest
import com.example.myapplication.model.FetchMessagesResponse
import com.example.myapplication.model.FileUploadResponse
import com.example.myapplication.model.GenerateConversationRequest
import com.example.myapplication.model.GenerateConversationResponse
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

    suspend fun signUp(cookie: String, name: String, phone: String, password: String, dateOfBrith: String, email: String): SignUpResponse {
        val request = SignUpRequest(name, phone, password, dateOfBrith, email)
        return apiService.signUp(cookie, request)
    }

    suspend fun generateConversation(adminId: Int, participants: List<Int>, conversationName: String): GenerateConversationResponse {
        val request = GenerateConversationRequest(adminId, participants, conversationName)
        return apiService.generateConversation(request)
    }

    suspend fun updateAvatar(userId: Int, extension: String, file: MultipartBody.Part): FileUploadResponse {
        val userIdRequest = RequestBody.create(MultipartBody.FORM, userId.toString())
        val extensionRequest = RequestBody.create(MultipartBody.FORM, extension)
        return apiService.updateAvatar(userIdRequest, extensionRequest, file)
    }
}
