package com.example.pet4you.network

import com.example.pet4you.data.model.ChatMessage
import retrofit2.http.Body
import retrofit2.http.POST

data class ChatRequest(val message: String, val history: List<ChatMessage>)
data class ChatResponse(val reply: String)

interface ApiService {
    @POST("chat")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse
}
