package com.example.pet4you.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet4you.data.model.ServiceProvider
import com.example.pet4you.repository.ServiceProviderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Loaded(val provider: ServiceProvider) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class ProfileActionState {
    object Idle : ProfileActionState()
    object Loading : ProfileActionState()
    object Success : ProfileActionState()
    data class Error(val message: String) : ProfileActionState()
}

class ServiceProviderViewModel : ViewModel() {

    private val repository = ServiceProviderRepository()

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState

    private val _profileActionState = MutableStateFlow<ProfileActionState>(ProfileActionState.Idle)
    val profileActionState: StateFlow<ProfileActionState> = _profileActionState

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            val result = repository.getProfile()
            _profileState.value = if (result.isSuccess) {
                ProfileState.Loaded(result.getOrNull()!!)
            } else {
                ProfileState.Error(result.exceptionOrNull()?.message ?: "Failed to load profile")
            }
        }
    }

    fun updateProfile(fullName: String, description: String, location: String, isAvailable: Boolean) {
        viewModelScope.launch {
            _profileActionState.value = ProfileActionState.Loading
            val result = repository.updateProfile(fullName, description, location, isAvailable)
            _profileActionState.value = if (result.isSuccess) {
                ProfileActionState.Success
            } else {
                ProfileActionState.Error(result.exceptionOrNull()?.message ?: "Failed to update profile")
            }
        }
    }

    fun resetActionState() {
        _profileActionState.value = ProfileActionState.Idle
    }
}
