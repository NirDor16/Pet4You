package com.example.pet4you.data.model

data class Reminder(
    val reminderId: String = "",
    val ownerId: String = "",
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

object ReminderType {
    const val MEDICATION = "MEDICATION"
    const val FEEDING = "FEEDING"
    const val CHECKUP = "CHECKUP"
    const val VACCINATION = "VACCINATION"
    const val GROOMING = "GROOMING"

    val all = listOf(MEDICATION, FEEDING, CHECKUP, VACCINATION, GROOMING)

    fun displayName(type: String) = when (type) {
        MEDICATION -> "Medication"
        FEEDING -> "Feeding"
        CHECKUP -> "Checkup"
        VACCINATION -> "Vaccination"
        GROOMING -> "Grooming"
        else -> type
    }
}

object ReminderFrequency {
    const val ONCE = "ONCE"
    const val DAILY = "DAILY"
    const val WEEKLY = "WEEKLY"
    const val MONTHLY = "MONTHLY"

    val all = listOf(ONCE, DAILY, WEEKLY, MONTHLY)

    fun displayName(frequency: String) = when (frequency) {
        ONCE -> "Once"
        DAILY -> "Daily"
        WEEKLY -> "Weekly"
        MONTHLY -> "Monthly"
        else -> frequency
    }
}
