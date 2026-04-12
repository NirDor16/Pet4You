package com.example.pet4you.data.model

data class ServiceProvider(
    val serviceProviderId: String = "",
    val providerType: String = ProviderType.VET,
    val fullName: String = "",
    val email: String = "",
    val description: String = "",
    val location: String = "",
    val isAvailable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

object ProviderType {
    const val VET = "VET"
    const val DOG_SITTER = "DOG_SITTER"
    const val GROOMER = "GROOMER"

    fun displayName(type: String): String = when (type) {
        VET -> "Veterinarian"
        DOG_SITTER -> "Dog Sitter"
        GROOMER -> "Groomer"
        else -> type
    }

    val all = listOf(VET, DOG_SITTER, GROOMER)
}
