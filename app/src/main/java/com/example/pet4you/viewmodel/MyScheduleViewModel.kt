package com.example.pet4you.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet4you.data.model.ServiceRequest
import com.example.pet4you.repository.ServiceRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ScheduleState {
    object Idle : ScheduleState()
    object Loading : ScheduleState()
    data class Success(
        val requests: List<ServiceRequest>,
        val ownerMap: Map<String, String>,
        val dogMap: Map<String, String>
    ) : ScheduleState()
    data class Error(val message: String) : ScheduleState()
}

class MyScheduleViewModel : ViewModel() {

    private val repository = ServiceRequestRepository()

    private val _state = MutableStateFlow<ScheduleState>(ScheduleState.Idle)
    val state: StateFlow<ScheduleState> = _state

    fun loadSchedule() {
        viewModelScope.launch {
            _state.value = ScheduleState.Loading
            val result = repository.getApprovedRequestsForCurrentProvider()
            if (result.isFailure) {
                _state.value = ScheduleState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to load schedule"
                )
                return@launch
            }
            val requests = result.getOrNull()!!.sortedBy { it.scheduledAt }
            val ownerIds = requests.map { it.dogOwnerId }.distinct()
            val dogIds = requests.map { it.dogId }.distinct()
            val ownerMap = repository.getOwnerNames(ownerIds)
            val dogMap = repository.getDogNames(dogIds)
            _state.value = ScheduleState.Success(requests, ownerMap, dogMap)
        }
    }
}
