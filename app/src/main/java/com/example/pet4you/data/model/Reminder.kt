package com.example.pet4you.data.model

data class Reminder(
    val reminderId: String = "",
    val dogId: String = "",
    val type: String = "",
    val dateTime: Long = 0L,
    val frequency: String = "",
    val status: String = ReminderStatus.ACTIVE
)

object ReminderStatus {
    const val ACTIVE = "ACTIVE"
    const val DONE = "DONE"
}
