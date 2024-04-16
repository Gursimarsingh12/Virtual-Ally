package com.chatbot.geminiai.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {
    @POST("/v1beta/models/gemini-pro:generateContent")
    suspend fun getApiResponse(@Header("Content-Type") contentType: String,
                        @Query("key") apiKey: String,
                        @Body response: ChatRequest
    ): Response<ChatResponse>
}