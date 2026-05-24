package com.example.pet4you.repository

import com.example.pet4you.network.ApiClient
import com.example.pet4you.network.WeatherResponse
import java.util.concurrent.ConcurrentHashMap

object WeatherRepository {

    private val cache = ConcurrentHashMap<String, WeatherResponse>()

    suspend fun getWeather(location: String): WeatherResponse? {
        val cacheKey = location.lowercase().trim()
        cache[cacheKey]?.let { return it }
        val city = bestCityQuery(location)
        return runCatching {
            ApiClient.apiService.getWeather(city)
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
