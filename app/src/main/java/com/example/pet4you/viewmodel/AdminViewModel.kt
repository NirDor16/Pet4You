package com.example.pet4you.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet4you.data.model.User
import com.example.pet4you.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AdminState {
    object Idle : AdminState()
    object Loading : AdminState()
    data class Success(val users: List<User>) : AdminState()
    data class Error(val message: String) : AdminState()
}

sealed class AdminActionState {
    object Idle : AdminActionState()
    object Loading : AdminActionState()
    object Success : AdminActionState()
    data class Error(val message: String) : AdminActionState()
}

class AdminViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _state = MutableStateFlow<AdminState>(AdminState.Idle)
    val state: StateFlow<AdminState> = _state

    private val _actionState = MutableStateFlow<AdminActionState>(AdminActionState.Idle)
    val actionState: StateFlow<AdminActionState> = _actionState

    fun loadUsers() {
        viewModelScope.launch {
            _state.value = AdminState.Loading
            val result = repository.getAllUsers()
            _state.value = if (result.isSuccess) {
                AdminState.Success(result.getOrNull()!!.sortedBy { it.createdAt })
            } else {
                AdminState.Error(result.exceptionOrNull()?.message ?: "Failed to load users")
            }
        }
    }

    fun setBlocked(uid: String, blocked: Boolean) {
        viewModelScope.launch {
            _actionState.value = AdminActionState.Loading
            val result = repository.setUserBlocked(uid, blocked)
            if (result.isSuccess) {
                _actionState.value = AdminActionState.Success
                val current = _state.value
                if (current is AdminState.Success) {
                    _state.value = AdminState.Success(
                        current.users.map { if (it.uid == uid) it.copy(isBlocked = blocked) else it }
                    )
                }
            } else {
                _actionState.value = AdminActionState.Error(
                    result.exceptionOrNull()?.message ?: "Action failed"
                )
            }
        }
    }

    fun resetActionState() {
        _actionState.value = AdminActionState.Idle
    }
}
