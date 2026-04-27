package com.example.pet4you.repository

import com.example.pet4you.data.model.ServiceProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ServiceProviderRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun getProfile(): Result<ServiceProvider> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            val doc = firestore.collection("serviceProviders").document(uid).get().await()
            Result.success(doc.toObject(ServiceProvider::class.java) ?: ServiceProvider())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllProviders(providerType: String? = null): Result<List<ServiceProvider>> {
        return try {
            val query = if (providerType != null) {
                firestore.collection("serviceProviders").whereEqualTo("providerType", providerType)
            } else {
                firestore.collection("serviceProviders")
            }
            val snapshot = query.get().await()
            Result.success(snapshot.documents.mapNotNull { it.toObject(ServiceProvider::class.java) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProviderById(providerId: String): Result<ServiceProvider> {
        return try {
            val doc = firestore.collection("serviceProviders").document(providerId).get().await()
            Result.success(doc.toObject(ServiceProvider::class.java) ?: ServiceProvider())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        fullName: String,
        description: String,
        location: String,
        isAvailable: Boolean
    ): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            firestore.collection("serviceProviders").document(uid).update(
                mapOf(
                    "fullName" to fullName,
                    "description" to description,
                    "location" to location,
                    "isAvailable" to isAvailable
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
