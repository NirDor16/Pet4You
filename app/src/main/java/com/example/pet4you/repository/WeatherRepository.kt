package com.example.pet4you.repository

import com.example.pet4you.BuildConfig
import com.example.pet4you.network.WeatherResponse
import com.example.pet4you.network.weatherApiService
import java.util.concurrent.ConcurrentHashMap

object WeatherRepository {

    private val cache = ConcurrentHashMap<String, WeatherResponse>()

    suspend fun getWeather(location: String): WeatherResponse? {
        val cacheKey = location.lowercase().trim()
        cache[cacheKey]?.let { return it }
        val query = bestCityQuery(location)
        return runCatching {
            weatherApiService.getCurrentWeather(query, apiKey = BuildConfig.OPENWEATHER_API_KEY)
        }.getOrNull()?.also { cache[cacheKey] = it }
    }

    private fun bestCityQuery(location: String): String {
        val parts = location.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        return when {
            parts.size >= 2 -> parts.last()
            else            -> parts.firstOrNull() ?: location
        }
    }
}
