package com.example.pet4you.network

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class SerpApiResponse(
    @SerializedName("local_results") val localResults: List<DogParkResult>?,
)

data class DogParkResult(
    @SerializedName("title")   val title:   String,
    @SerializedName("address") val address: String?,
    @SerializedName("rating")  val rating:  Double?,
    @SerializedName("reviews") val reviews: Int?,
    @SerializedName("type")    val type:    String?,
)

interface SerpApiService {
    @GET("search.json")
    suspend fun searchDogParks(
        @Query("engine")  engine: String = "google_maps",
        @Query("q")       query:  String = "dog park",
        @Query("ll")      ll:     String,
        @Query("type")    type:   String = "search",
        @Query("api_key") apiKey: String,
    ): SerpApiResponse
}

val serpApiService: SerpApiService = Retrofit.Builder()
    .baseUrl("https://serpapi.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(SerpApiService::class.java)
