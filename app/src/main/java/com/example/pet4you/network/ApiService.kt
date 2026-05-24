package com.example.pet4you.network

import com.example.pet4you.data.model.ChatMessage
import com.example.pet4you.data.model.Meetup
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class ChatRequest(val message: String, val history: List<ChatMessage>)
data class ChatResponse(val reply: String)

data class RecommendMeetupsRequest(
    @SerializedName("dog_breeds") val dogBreeds: List<String>,
    @SerializedName("user_id")    val userId:    String,
    @SerializedName("meetups")    val meetups:   List<Meetup>,
)
data class RecommendMeetupsResponse(
    @SerializedName("recommendations") val recommendations: List<Meetup>,
)

// Weather
data class WeatherResponse(
    @SerializedName("main")    val main:     WeatherMain,
    @SerializedName("weather") val weather:  List<WeatherCondition>,
    @SerializedName("wind")    val wind:     WeatherWind,
    @SerializedName("name")    val cityName: String,
)
data class WeatherMain(
    @SerializedName("temp")     val temp:     Double,
    @SerializedName("humidity") val humidity: Int,
)
data class WeatherCondition(
    @SerializedName("description") val description: String,
    @SerializedName("icon")        val icon:        String,
)
data class WeatherWind(
    @SerializedName("speed") val speed: Double,
)

// Dog parks
data class DogParksRequest(val lat: Double, val lon: Double)
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

interface ApiService {
    @POST("chat")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse

    @POST("recommend-meetups")
    suspend fun recommendMeetups(@Body request: RecommendMeetupsRequest): RecommendMeetupsResponse

    @GET("weather")
    suspend fun getWeather(@Query("location") location: String): WeatherResponse

    @GET("weather-forecast")
    suspend fun getWeatherForecast(
        @Query("location")    location:   String,
        @Query("datetime_ms") datetimeMs: Long,
    ): WeatherResponse

    @POST("dog-parks")
    suspend fun getDogParks(@Body request: DogParksRequest): SerpApiResponse
}
