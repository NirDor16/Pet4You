package com.example.pet4you.network

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

data class DogCeoResponse(
    @SerializedName("message") val imageUrl: String,
    @SerializedName("status") val status: String,
)

interface DogCeoApiService {
    @GET("breed/{path}/images/random")
    suspend fun getRandomBreedImage(
        @Path("path", encoded = true) breedPath: String,
    ): DogCeoResponse

    @GET("breeds/image/random")
    suspend fun getRandomImage(): DogCeoResponse
}

val dogCeoApiService: DogCeoApiService = Retrofit.Builder()
    .baseUrl("https://dog.ceo/api/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(DogCeoApiService::class.java)
