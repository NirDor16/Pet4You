package com.example.pet4you.network

import com.example.pet4you.data.model.ChatMessage
import com.example.pet4you.data.model.Meetup
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

data class ChatRequest(val message: String, val history: List<ChatMessage>)
data class ChatResponse(val reply: String)

data class RecommendMeetupsRequest(
    @SerializedName("dog_breeds") val dogBreeds: List<String>,
    @SerializedName("user_id") val userId: String,
    @SerializedName("meetups") val meetups: List<Meetup>
)

data class RecommendMeetupsResponse(
    @SerializedName("recommendations") val recommendations: List<Meetup>
)

interface ApiService {
    @POST("chat")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse

    @POST("recommend-meetups")
    suspend fun recommendMeetups(@Body request: RecommendMeetupsRequest): RecommendMeetupsResponse
}
