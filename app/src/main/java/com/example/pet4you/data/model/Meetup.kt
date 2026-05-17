package com.example.pet4you.data.model

import com.google.firebase.firestore.Exclude

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
    val recommendationScore: Float? = null,
)
