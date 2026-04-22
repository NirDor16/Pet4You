package com.example.pet4you.repository

import com.example.pet4you.data.model.Dog
import com.example.pet4you.data.model.Reminder
import com.example.pet4you.data.model.ReminderStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReminderRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun getRemindersForCurrentUser(): Result<List<Reminder>> {
        return try {
            val uid = currentUserId ?: return Result.failure(Exception("User not logged in"))
            val snapshot = firestore.collection("reminders")
                .whereEqualTo("ownerId", uid)
                .get()
                .await()
            val reminders = snapshot.documents.mapNotNull { it.toObject(Reminder::class.java) }
            Result.success(reminders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addReminder(dogId: String, type: String, dateTime: Long, frequency: String): Result<Unit> {
        return try {
            val uid = currentUserId ?: return Result.failure(Exception("User not logged in"))
            val docRef = firestore.collection("reminders").document()
            val reminder = Reminder(
                reminderId = docRef.id,
                ownerId = uid,
                dogId = dogId,
                type = type,
                dateTime = dateTime,
                frequency = frequency,
                status = ReminderStatus.ACTIVE
            )
            docRef.set(reminder).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReminder(reminderId: String): Result<Reminder> {
        return try {
            val doc = firestore.collection("reminders").document(reminderId).get().await()
            val reminder = doc.toObject(Reminder::class.java)
                ?: return Result.failure(Exception("Reminder not found"))
            Result.success(reminder)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReminder(
        reminderId: String, dogId: String, type: String,
        dateTime: Long, frequency: String, status: String
    ): Result<Unit> {
        return try {
            val updates = mapOf(
                "dogId" to dogId,
                "type" to type,
                "dateTime" to dateTime,
                "frequency" to frequency,
                "status" to status
            )
            firestore.collection("reminders").document(reminderId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReminder(reminderId: String): Result<Unit> {
        return try {
            firestore.collection("reminders").document(reminderId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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
}
