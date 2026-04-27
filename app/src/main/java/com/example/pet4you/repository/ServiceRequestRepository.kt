package com.example.pet4you.repository

import com.example.pet4you.data.model.ServiceRequest
import com.example.pet4you.data.model.RequestStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ServiceRequestRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun createRequest(
        serviceProviderId: String,
        dogId: String,
        providerType: String,
        message: String
    ): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            val ref = firestore.collection("serviceRequests").document()
            val request = ServiceRequest(
                requestId = ref.id,
                dogOwnerId = uid,
                serviceProviderId = serviceProviderId,
                dogId = dogId,
                providerType = providerType,
                message = message,
                status = RequestStatus.PENDING
            )
            ref.set(request).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRequestsForCurrentProvider(): Result<List<ServiceRequest>> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            val snapshot = firestore.collection("serviceRequests")
                .whereEqualTo("serviceProviderId", uid)
                .get().await()
            Result.success(snapshot.documents.mapNotNull { it.toObject(ServiceRequest::class.java) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRequestStatus(requestId: String, status: String): Result<Unit> {
        return try {
            firestore.collection("serviceRequests").document(requestId)
                .update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOwnerNames(ownerIds: List<String>): Map<String, String> {
        if (ownerIds.isEmpty()) return emptyMap()
        return try {
            ownerIds.associateWith { uid ->
                val doc = firestore.collection("users").document(uid).get().await()
                doc.getString("fullName") ?: uid
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun getDogNames(dogIds: List<String>): Map<String, String> {
        if (dogIds.isEmpty()) return emptyMap()
        return try {
            dogIds.associateWith { dogId ->
                val doc = firestore.collection("dogs").document(dogId).get().await()
                doc.getString("name") ?: dogId
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
