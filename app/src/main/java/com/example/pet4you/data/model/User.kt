package com.example.pet4you.data.model

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: String = UserRole.DOG_OWNER,
    val isBlocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

object UserRole {
    const val DOG_OWNER = "DOG_OWNER"
    const val SERVICE_PROVIDER = "SERVICE_PROVIDER"
    const val ADMIN = "ADMIN"
}
