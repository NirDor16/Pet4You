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
        message: String,
        scheduledAt: Long
    ): Result<ServiceRequest> {
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
                status = RequestStatus.PENDING,
                scheduledAt = scheduledAt
            )
            ref.set(request).await()
            Result.success(request)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActiveRequestToProvider(serviceProviderId: String): Result<ServiceRequest?> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            val snapshot = firestore.collection("serviceRequests")
                .whereEqualTo("dogOwnerId", uid)
                .whereEqualTo("serviceProviderId", serviceProviderId)
                .get().await()
            val active = snapshot.documents
                .mapNotNull { it.toObject(ServiceRequest::class.java) }
                .firstOrNull { it.status != RequestStatus.REJECTED }
            Result.success(active)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActiveRequestsForProvider(serviceProviderId: String): Result<List<ServiceRequest>> {
        return try {
            val snapshot = firestore.collection("serviceRequests")
                .whereEqualTo("serviceProviderId", serviceProviderId)
                .get().await()
            val active = snapshot.documents
                .mapNotNull { it.toObject(ServiceRequest::class.java) }
                .filter { it.status != RequestStatus.REJECTED }
            Result.success(active)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getApprovedRequestsForCurrentOwner(): Result<List<ServiceRequest>> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            val snapshot = firestore.collection("serviceRequests")
                .whereEqualTo("dogOwnerId", uid)
                .whereEqualTo("status", RequestStatus.APPROVED)
                .get().await()
            Result.success(snapshot.documents.mapNotNull { it.toObject(ServiceRequest::class.java) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markReminderCreated(requestId: String): Result<Unit> {
        return try {
            firestore.collection("serviceRequests").document(requestId)
                .update("reminderCreated", true).await()
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

    suspend fun updateRequestStatus(
        requestId: String,
        status: String,
        scheduledAt: Long? = null
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>("status" to status)
            if (scheduledAt != null) updates["scheduledAt"] = scheduledAt
            firestore.collection("serviceRequests").document(requestId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getApprovedRequestsForCurrentProvider(): Result<List<ServiceRequest>> {
        val uid = currentUserId ?: return Result.failure(Exception("Not logged in"))
        return try {
            val snapshot = firestore.collection("serviceRequests")
                .whereEqualTo("serviceProviderId", uid)
                .whereEqualTo("status", RequestStatus.APPROVED)
                .get().await()
            Result.success(snapshot.documents.mapNotNull { it.toObject(ServiceRequest::class.java) })
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
