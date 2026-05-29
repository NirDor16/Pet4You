package com.example.pet4you.data.model

import com.google.firebase.firestore.Exclude

data class Dog(
    val dogId:     String     = "",
    val ownerId:   String     = "",
    val name:      String     = "",
    val breed:     String     = "",
    val birthDate: String     = "",
    val notes:     String     = "",
    val photoUrl:  String     = "",
    @get:Exclude val avatar: DogAvatar? = null
)
