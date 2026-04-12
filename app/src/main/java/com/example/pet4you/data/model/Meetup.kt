package com.example.pet4you.data.model

data class Meetup(
    val meetupId: String = "",
    val creatorId: String = "",
    val location: String = "",
    val dateTime: Long = 0L,
    val description: String = "",
    val participants: List<String> = emptyList(),
    val dogBreeds: List<String> = emptyList()
)
