package com.example.pet4you.repository

import com.example.pet4you.BuildConfig
import com.example.pet4you.network.DogParkResult
import com.example.pet4you.network.serpApiService

object DogParkRepository {
    suspend fun getNearbyDogParks(lat: Double, lon: Double): List<DogParkResult> {
        val ll = "@$lat,$lon,15z"
        return runCatching {
            serpApiService.searchDogParks(ll = ll, apiKey = BuildConfig.SERP_API_KEY)
                .localResults ?: emptyList()
        }.getOrElse { emptyList() }
    }
}
