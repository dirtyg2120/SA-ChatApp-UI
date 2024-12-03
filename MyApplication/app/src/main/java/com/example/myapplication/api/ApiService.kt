package com.example.myapplication.api

import com.example.myapplication.model.FetchMessagesRequest
import com.example.myapplication.model.FetchMessagesResponse
import com.example.myapplication.model.FileUploadResponse
import com.example.myapplication.model.FindUserRequest
import com.example.myapplication.model.FindUserResponse
import com.example.myapplication.model.GenerateConversationRequest
import com.example.myapplication.model.GenerateConversationResponse
import com.example.myapplication.model.LoginRequest
import com.example.myapplication.model.LoginResponse
import com.example.myapplication.model.SignUpRequest
import com.example.myapplication.model.SignUpResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @POST("api/login")
    @Headers("Content-Type: application/json")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/conversation/fetch")
    @Headers("Content-Type: application/json")
    suspend fun fetchMessages(
        @Body request: FetchMessagesRequest,
        @Header("Cookie") cookie: String
    ): FetchMessagesResponse

    @Multipart
    @POST("api/file/upload")
    suspend fun uploadFile(
        @Header("Cookie") cookie: String,
        @Part("contentType") contentType: RequestBody,
        @Part("extension") extension: RequestBody,
        @Part file: MultipartBody.Part
    ): FileUploadResponse

    @POST("api/signUp")
    @Headers("Content-Type: application/json")
    suspend fun signUp(
        @Header("Cookie") cookie: String,
        @Body request: SignUpRequest
    ): SignUpResponse

    @POST("api/conversation/generate")
    @Headers("Content-Type: application/json")
    suspend fun generateConversation(
        @Body request: GenerateConversationRequest
    ): GenerateConversationResponse

    @Multipart
    @POST("api/update/avatar")
    suspend fun updateAvatar(
        @Part("userId") userId: RequestBody,
        @Part("extension") extension: RequestBody,
        @Part file: MultipartBody.Part
    ): FileUploadResponse

    @POST("api/user/find")
    @Headers("Content-Type: application/json")
    suspend fun findUser(
        @Body request: FindUserRequest
    ): FindUserResponse
}


// Retrofit Singleton Instance
object RetrofitInstance {
    private const val BASE_URL = "http://128.199.91.226:8082/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}