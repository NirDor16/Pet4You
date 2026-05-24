package com.example.pet4you.repository

import com.example.pet4you.network.ApiClient
import com.example.pet4you.network.DogParkResult
import com.example.pet4you.network.DogParksRequest

object DogParkRepository {
    suspend fun getNearbyDogParks(lat: Double, lon: Double): List<DogParkResult> =
        runCatching {
            ApiClient.apiService.getDogParks(DogParksRequest(lat, lon)).localResults ?: emptyList()
        }.getOrElse { emptyList() }
}
