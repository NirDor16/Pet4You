package com.example.pet4you.repository

import com.example.pet4you.data.model.Meetup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MeetupRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun getAllMeetups(): Result<List<Meetup>> {
        return try {
            val snapshot = firestore.collection("meetups").get().await()
            val meetups = snapshot.documents.mapNotNull { it.toObject(Meetup::class.java) }
            Result.success(meetups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createMeetup(
        location: String,
        dateTime: Long,
        description: String,
        dogBreeds: List<String>
    ): Result<Unit> {
        return try {
            val uid = currentUserId ?: return Result.failure(Exception("User not logged in"))
            val docRef = firestore.collection("meetups").document()
            val meetup = Meetup(
                meetupId = docRef.id,
                creatorId = uid,
                location = location,
                dateTime = dateTime,
                description = description,
                participants = listOf(uid),
                dogBreeds = dogBreeds
            )
            docRef.set(meetup).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinMeetup(meetupId: String): Result<Unit> {
        return try {
            val uid = currentUserId ?: return Result.failure(Exception("User not logged in"))
            firestore.collection("meetups").document(meetupId)
                .update("participants", FieldValue.arrayUnion(uid))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun leaveMeetup(meetupId: String): Result<Unit> {
        return try {
            val uid = currentUserId ?: return Result.failure(Exception("User not logged in"))
            firestore.collection("meetups").document(meetupId)
                .update("participants", FieldValue.arrayRemove(uid))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMeetup(meetupId: String): Result<Unit> {
        return try {
            firestore.collection("meetups").document(meetupId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
