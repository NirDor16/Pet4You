package com.example.pet4you.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet4you.data.model.Dog
import com.example.pet4you.repository.DogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DogListState {
    object Idle : DogListState()
    object Loading : DogListState()
    data class Success(val dogs: List<Dog>) : DogListState()
    data class Error(val message: String) : DogListState()
}

sealed class DogActionState {
    object Idle : DogActionState()
    object Loading : DogActionState()
    object Success : DogActionState()
    data class DogLoaded(val dog: Dog) : DogActionState()
    data class Error(val message: String) : DogActionState()
}

class DogViewModel : ViewModel() {

    private val repository = DogRepository()

    private val _dogListState = MutableStateFlow<DogListState>(DogListState.Idle)
    val dogListState: StateFlow<DogListState> = _dogListState

    private val _dogActionState = MutableStateFlow<DogActionState>(DogActionState.Idle)
    val dogActionState: StateFlow<DogActionState> = _dogActionState

    fun loadDogs() {
        viewModelScope.launch {
            _dogListState.value = DogListState.Loading
            val result = repository.getDogsForCurrentUser()
            _dogListState.value = if (result.isSuccess) {
                DogListState.Success(result.getOrNull()!!)
            } else {
                DogListState.Error(result.exceptionOrNull()?.message ?: "Failed to load dogs")
            }
        }
    }

    fun loadDog(dogId: String) {
        viewModelScope.launch {
            _dogActionState.value = DogActionState.Loading
            val result = repository.getDog(dogId)
            _dogActionState.value = if (result.isSuccess) {
                DogActionState.DogLoaded(result.getOrNull()!!)
            } else {
                DogActionState.Error(result.exceptionOrNull()?.message ?: "Failed to load dog")
            }
        }
    }

    fun addDog(name: String, breed: String, birthDate: String, notes: String) {
        viewModelScope.launch {
            _dogActionState.value = DogActionState.Loading
            val result = repository.addDog(name, breed, birthDate, notes)
            _dogActionState.value = if (result.isSuccess) {
                DogActionState.Success
            } else {
                DogActionState.Error(result.exceptionOrNull()?.message ?: "Failed to add dog")
            }
        }
    }

    fun updateDog(dogId: String, name: String, breed: String, birthDate: String, notes: String) {
        viewModelScope.launch {
            _dogActionState.value = DogActionState.Loading
            val result = repository.updateDog(dogId, name, breed, birthDate, notes)
            _dogActionState.value = if (result.isSuccess) {
                DogActionState.Success
            } else {
                DogActionState.Error(result.exceptionOrNull()?.message ?: "Failed to update dog")
            }
        }
    }

    fun deleteDog(dogId: String) {
        viewModelScope.launch {
            _dogActionState.value = DogActionState.Loading
            val result = repository.deleteDog(dogId)
            if (result.isSuccess) {
                loadDogs()
            } else {
                _dogActionState.value = DogActionState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to delete dog"
                )
            }
        }
    }

    fun resetActionState() {
        _dogActionState.value = DogActionState.Idle
    }
}
