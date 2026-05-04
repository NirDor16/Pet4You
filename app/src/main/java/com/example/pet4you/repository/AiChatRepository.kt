package com.example.pet4you.repository

import com.example.pet4you.data.model.ChatMessage
import com.example.pet4you.network.ApiClient
import com.example.pet4you.network.ChatRequest

class AiChatRepository {
    suspend fun sendMessage(message: String, history: List<ChatMessage>): Result<String> {
        return try {
            val response = ApiClient.apiService.sendMessage(
                ChatRequest(message = message, history = history)
            )
            Result.success(response.reply)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
