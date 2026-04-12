package com.example.pet4you.data.model

data class ServiceRequest(
    val requestId: String = "",
    val dogOwnerId: String = "",
    val serviceProviderId: String = "",
    val dogId: String = "",
    val providerType: String = "",
    val message: String = "",
    val status: String = RequestStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

object RequestStatus {
    const val PENDING = "PENDING"
    const val APPROVED = "APPROVED"
    const val REJECTED = "REJECTED"
}
