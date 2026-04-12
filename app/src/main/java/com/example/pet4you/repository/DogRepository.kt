package com.example.pet4you.repository

import com.example.pet4you.data.model.Dog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DogRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun getDogsForCurrentUser(): Result<List<Dog>> {
        return try {
            val uid = currentUserId ?: return Result.failure(Exception("User not logged in"))
            val snapshot = firestore.collection("dogs")
                .whereEqualTo("ownerId", uid)
                .get()
                .await()
            val dogs = snapshot.documents.mapNotNull { it.toObject(Dog::class.java) }
            Result.success(dogs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addDog(name: String, breed: String, birthDate: String, notes: String): Result<Unit> {
        return try {
            val uid = currentUserId ?: return Result.failure(Exception("User not logged in"))
            val docRef = firestore.collection("dogs").document()
            val dog = Dog(
                dogId = docRef.id,
                ownerId = uid,
                name = name,
                breed = breed,
                birthDate = birthDate,
                notes = notes
            )
            docRef.set(dog).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDog(dogId: String): Result<Dog> {
        return try {
            val doc = firestore.collection("dogs").document(dogId).get().await()
            val dog = doc.toObject(Dog::class.java)
                ?: return Result.failure(Exception("Dog not found"))
            Result.success(dog)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDog(dogId: String, name: String, breed: String, birthDate: String, notes: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "name" to name,
                "breed" to breed,
                "birthDate" to birthDate,
                "notes" to notes
            )
            firestore.collection("dogs").document(dogId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDog(dogId: String): Result<Unit> {
        return try {
            firestore.collection("dogs").document(dogId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
