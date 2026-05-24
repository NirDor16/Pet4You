package com.example.pet4you.network

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

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

interface WeatherApiService {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("q")     city:   String,
        @Query("units") units:  String = "metric",
        @Query("appid") apiKey: String,
    ): WeatherResponse
}

val weatherApiService: WeatherApiService = Retrofit.Builder()
    .baseUrl("https://api.openweathermap.org/data/2.5/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(WeatherApiService::class.java)
