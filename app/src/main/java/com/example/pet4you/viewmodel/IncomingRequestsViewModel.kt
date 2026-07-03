package com.example.pet4you.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet4you.data.model.RequestStatus
import com.example.pet4you.data.model.ServiceRequest
import com.example.pet4you.repository.ServiceRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class IncomingRequestsState {
    object Idle : IncomingRequestsState()
    object Loading : IncomingRequestsState()
    data class Success(
        val requests: List<ServiceRequest>,
        val ownerMap: Map<String, String>,
        val dogMap: Map<String, String>
    ) : IncomingRequestsState()
    data class Error(val message: String) : IncomingRequestsState()
}

sealed class RequestActionState {
    object Idle : RequestActionState()
    object Loading : RequestActionState()
    object Success : RequestActionState()
    data class Error(val message: String) : RequestActionState()
}

class IncomingRequestsViewModel : ViewModel() {

    private val repository = ServiceRequestRepository()

    private val _listState = MutableStateFlow<IncomingRequestsState>(IncomingRequestsState.Idle)
    val listState: StateFlow<IncomingRequestsState> = _listState

    private val _actionState = MutableStateFlow<RequestActionState>(RequestActionState.Idle)
    val actionState: StateFlow<RequestActionState> = _actionState

    fun loadRequests() {
        viewModelScope.launch {
            _listState.value = IncomingRequestsState.Loading
            val result = repository.getRequestsForCurrentProvider()
            if (result.isFailure) {
                _listState.value = IncomingRequestsState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to load requests"
                )
                return@launch
            }
            val requests = result.getOrNull()!!
            val ownerIds = requests.map { it.dogOwnerId }.distinct()
            val dogIds = requests.map { it.dogId }.distinct()
            val ownerMap = repository.getOwnerNames(ownerIds)
            val dogMap = repository.getDogNames(dogIds)
            _listState.value = IncomingRequestsState.Success(requests, ownerMap, dogMap)
        }
    }

    fun approveRequest(requestId: String) {
        viewModelScope.launch {
            _actionState.value = RequestActionState.Loading
            val result = repository.updateRequestStatus(requestId, RequestStatus.APPROVED)
            if (result.isSuccess) {
                loadRequests()
            } else {
                _actionState.value = RequestActionState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to approve request"
                )
            }
        }
    }

    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            _actionState.value = RequestActionState.Loading
            val result = repository.updateRequestStatus(requestId, RequestStatus.REJECTED)
            if (result.isSuccess) {
                loadRequests()
            } else {
                _actionState.value = RequestActionState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to reject request"
                )
            }
        }
    }

    fun resetActionState() {
        _actionState.value = RequestActionState.Idle
    }
}
