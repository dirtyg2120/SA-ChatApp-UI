package com.example.myapplication.api

import com.example.myapplication.model.FetchMessagesRequest
import com.example.myapplication.model.FetchMessagesResponse
import com.example.myapplication.model.LoginRequest
import com.example.myapplication.model.LoginResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

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