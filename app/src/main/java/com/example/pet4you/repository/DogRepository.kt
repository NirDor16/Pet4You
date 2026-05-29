package com.example.pet4you.repository

import android.net.Uri
import com.example.pet4you.data.model.Dog
import com.example.pet4you.data.model.DogAvatar
import com.example.pet4you.data.model.toDogAvatar
import com.example.pet4you.data.model.toMap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class DogRepository {

    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    @Suppress("UNCHECKED_CAST")
    private fun attachAvatar(doc: DocumentSnapshot, dog: Dog): Dog {
        val raw = doc.get("avatar") as? Map<String, Any> ?: return dog
        return dog.copy(avatar = raw.toDogAvatar())
    }

    suspend fun getDogsForCurrentUser(): Result<List<Dog>> = runCatching {
        val uid = currentUserId ?: throw Exception("User not logged in")
        firestore.collection("dogs")
            .whereEqualTo("ownerId", uid)
            .get().await()
            .documents.mapNotNull { doc ->
                doc.toObject(Dog::class.java)?.let { attachAvatar(doc, it) }
            }
    }

    suspend fun uploadDogPhoto(imageUri: Uri): Result<String> = runCatching {
        val userId = currentUserId ?: throw Exception("Not logged in")
        val ref = FirebaseStorage.getInstance().reference
            .child("dog_photos/$userId/${UUID.randomUUID()}.jpg")
        ref.putFile(imageUri).await()
        ref.downloadUrl.await().toString()
    }

    suspend fun addDog(
        name: String, breed: String, birthDate: String,
        notes: String, photoUrl: String = "", avatar: DogAvatar? = null
    ): Result<Unit> = runCatching {
        val uid    = currentUserId ?: throw Exception("User not logged in")
        val docRef = firestore.collection("dogs").document()
        val data   = mutableMapOf<String, Any>(
            "dogId"     to docRef.id,
            "ownerId"   to uid,
            "name"      to name,
            "breed"     to breed,
            "birthDate" to birthDate,
            "notes"     to notes,
            "photoUrl"  to photoUrl
        )
        avatar?.let { data["avatar"] = it.toMap() }
        docRef.set(data).await()
    }

    suspend fun getDog(dogId: String): Result<Dog> = runCatching {
        val doc = firestore.collection("dogs").document(dogId).get().await()
        val dog = doc.toObject(Dog::class.java) ?: throw Exception("Dog not found")
        attachAvatar(doc, dog)
    }

    // avatar: pass non-null to write/overwrite; null = leave existing avatar field untouched
    suspend fun updateDog(
        dogId: String, name: String, breed: String,
        birthDate: String, notes: String,
        photoUrl: String = "", avatar: DogAvatar? = null
    ): Result<Unit> = runCatching {
        val updates = mutableMapOf<String, Any>(
            "name"      to name,
            "breed"     to breed,
            "birthDate" to birthDate,
            "notes"     to notes
        )
        if (photoUrl.isNotEmpty()) updates["photoUrl"] = photoUrl
        avatar?.let { updates["avatar"] = it.toMap() }
        firestore.collection("dogs").document(dogId).update(updates).await()
    }

    suspend fun saveAvatar(dogId: String, avatar: DogAvatar): Result<Unit> = runCatching {
        firestore.collection("dogs").document(dogId)
            .update("avatar", avatar.toMap()).await()
    }

    suspend fun deleteDog(dogId: String): Result<Unit> = runCatching {
        firestore.collection("dogs").document(dogId).delete().await()
    }
}
