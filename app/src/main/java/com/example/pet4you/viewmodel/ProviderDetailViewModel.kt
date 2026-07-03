package com.example.pet4you.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet4you.data.model.Dog
import com.example.pet4you.data.model.ServiceProvider
import com.example.pet4you.data.model.ServiceRequest
import com.example.pet4you.repository.DogRepository
import com.example.pet4you.repository.ServiceProviderRepository
import com.example.pet4you.repository.ServiceRequestRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ProviderDetailState {
    object Idle : ProviderDetailState()
    object Loading : ProviderDetailState()
    data class Loaded(
        val provider: ServiceProvider,
        val dogs: List<Dog>,
        val existingRequest: ServiceRequest? = null,
    ) : ProviderDetailState()
    data class Error(val message: String) : ProviderDetailState()
}

sealed class SendRequestState {
    object Idle : SendRequestState()
    object Loading : SendRequestState()
    object Success : SendRequestState()
    data class Error(val message: String) : SendRequestState()
}

class ProviderDetailViewModel : ViewModel() {

    private val providerRepository = ServiceProviderRepository()
    private val dogRepository = DogRepository()
    private val requestRepository = ServiceRequestRepository()

    private val _detailState = MutableStateFlow<ProviderDetailState>(ProviderDetailState.Idle)
    val detailState: StateFlow<ProviderDetailState> = _detailState

    private val _sendRequestState = MutableStateFlow<SendRequestState>(SendRequestState.Idle)
    val sendRequestState: StateFlow<SendRequestState> = _sendRequestState

    fun load(providerId: String) {
        viewModelScope.launch {
            _detailState.value = ProviderDetailState.Loading
            val providerDeferred = async { providerRepository.getProviderById(providerId) }
            val dogsDeferred = async { dogRepository.getDogsForCurrentUser() }
            val existingRequestDeferred = async { requestRepository.getActiveRequestToProvider(providerId) }

            val providerResult = providerDeferred.await()
            val dogsResult = dogsDeferred.await()
            val existingRequestResult = existingRequestDeferred.await()

            _detailState.value = if (providerResult.isSuccess) {
                ProviderDetailState.Loaded(
                    provider = providerResult.getOrNull()!!,
                    dogs = dogsResult.getOrNull() ?: emptyList(),
                    existingRequest = existingRequestResult.getOrNull(),
                )
            } else {
                ProviderDetailState.Error(providerResult.exceptionOrNull()?.message ?: "Failed to load provider")
            }
        }
    }

    fun sendRequest(
        serviceProviderId: String,
        dogId: String,
        providerType: String,
        message: String
    ) {
        viewModelScope.launch {
            _sendRequestState.value = SendRequestState.Loading
            val result = requestRepository.createRequest(serviceProviderId, dogId, providerType, message)
            if (result.isSuccess) {
                _sendRequestState.value = SendRequestState.Success
                val current = _detailState.value
                if (current is ProviderDetailState.Loaded) {
                    _detailState.value = current.copy(existingRequest = result.getOrNull())
                }
            } else {
                _sendRequestState.value = SendRequestState.Error(result.exceptionOrNull()?.message ?: "Failed to send request")
            }
        }
    }

    fun resetSendState() {
        _sendRequestState.value = SendRequestState.Idle
    }
}
