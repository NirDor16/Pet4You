package com.example.pet4you.data.model

import com.google.firebase.firestore.Exclude
import com.google.gson.annotations.SerializedName

data class Meetup(
    val meetupId: String = "",
    val creatorId: String = "",
    val title: String = "",
    val location: String = "",
    val dateTime: Long = 0L,
    val description: String = "",
    val participants: List<String> = emptyList(),
    val dogBreeds: List<String> = emptyList(),
    val participantLimit: Int = 0,
    val createdAt: Long = 0L,
    @get:Exclude
    @SerializedName("score")
    val recommendationScore: Float? = null,
)
