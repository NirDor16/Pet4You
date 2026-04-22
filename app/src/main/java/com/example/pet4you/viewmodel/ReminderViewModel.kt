package com.example.pet4you.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet4you.data.model.Dog
import com.example.pet4you.data.model.Reminder
import com.example.pet4you.data.model.ReminderStatus
import com.example.pet4you.repository.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ReminderListState {
    object Idle : ReminderListState()
    object Loading : ReminderListState()
    data class Success(val reminders: List<Reminder>, val dogMap: Map<String, String>) : ReminderListState()
    data class Error(val message: String) : ReminderListState()
}

sealed class ReminderActionState {
    object Idle : ReminderActionState()
    object Loading : ReminderActionState()
    object Success : ReminderActionState()
    data class ReminderLoaded(val reminder: Reminder) : ReminderActionState()
    data class Error(val message: String) : ReminderActionState()
}

class ReminderViewModel : ViewModel() {

    private val repository = ReminderRepository()

    private val _reminderListState = MutableStateFlow<ReminderListState>(ReminderListState.Idle)
    val reminderListState: StateFlow<ReminderListState> = _reminderListState

    private val _reminderActionState = MutableStateFlow<ReminderActionState>(ReminderActionState.Idle)
    val reminderActionState: StateFlow<ReminderActionState> = _reminderActionState

    private val _dogs = MutableStateFlow<List<Dog>>(emptyList())
    val dogs: StateFlow<List<Dog>> = _dogs

    fun loadReminders() {
        viewModelScope.launch {
            _reminderListState.value = ReminderListState.Loading
            val dogsResult = repository.getDogsForCurrentUser()
            val remindersResult = repository.getRemindersForCurrentUser()
            _reminderListState.value = if (remindersResult.isSuccess) {
                val dogMap = dogsResult.getOrNull()?.associate { it.dogId to it.name } ?: emptyMap()
                ReminderListState.Success(
                    reminders = remindersResult.getOrNull()!!,
                    dogMap = dogMap
                )
            } else {
                ReminderListState.Error(
                    remindersResult.exceptionOrNull()?.message ?: "Failed to load reminders"
                )
            }
        }
    }

    fun loadDogs() {
        viewModelScope.launch {
            val result = repository.getDogsForCurrentUser()
            if (result.isSuccess) _dogs.value = result.getOrNull()!!
        }
    }

    fun loadReminder(reminderId: String) {
        viewModelScope.launch {
            _reminderActionState.value = ReminderActionState.Loading
            val result = repository.getReminder(reminderId)
            _reminderActionState.value = if (result.isSuccess) {
                ReminderActionState.ReminderLoaded(result.getOrNull()!!)
            } else {
                ReminderActionState.Error(result.exceptionOrNull()?.message ?: "Failed to load reminder")
            }
        }
    }

    fun addReminder(dogId: String, type: String, dateTime: Long, frequency: String) {
        viewModelScope.launch {
            _reminderActionState.value = ReminderActionState.Loading
            val result = repository.addReminder(dogId, type, dateTime, frequency)
            _reminderActionState.value = if (result.isSuccess) {
                ReminderActionState.Success
            } else {
                ReminderActionState.Error(result.exceptionOrNull()?.message ?: "Failed to add reminder")
            }
        }
    }

    fun updateReminder(
        reminderId: String, dogId: String, type: String,
        dateTime: Long, frequency: String, status: String
    ) {
        viewModelScope.launch {
            _reminderActionState.value = ReminderActionState.Loading
            val result = repository.updateReminder(reminderId, dogId, type, dateTime, frequency, status)
            _reminderActionState.value = if (result.isSuccess) {
                ReminderActionState.Success
            } else {
                ReminderActionState.Error(result.exceptionOrNull()?.message ?: "Failed to update reminder")
            }
        }
    }

    fun deleteReminder(reminderId: String) {
        viewModelScope.launch {
            _reminderActionState.value = ReminderActionState.Loading
            val result = repository.deleteReminder(reminderId)
            if (result.isSuccess) {
                loadReminders()
            } else {
                _reminderActionState.value = ReminderActionState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to delete reminder"
                )
            }
        }
    }

    fun toggleStatus(reminder: Reminder) {
        val newStatus = if (reminder.status == ReminderStatus.ACTIVE) ReminderStatus.DONE else ReminderStatus.ACTIVE
        viewModelScope.launch {
            val result = repository.updateReminder(
                reminder.reminderId, reminder.dogId, reminder.type,
                reminder.dateTime, reminder.frequency, newStatus
            )
            if (result.isSuccess) loadReminders()
        }
    }

    fun resetActionState() {
        _reminderActionState.value = ReminderActionState.Idle
    }
}
