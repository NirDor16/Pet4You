package com.example.pet4you.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet4you.data.model.Meetup
import com.example.pet4you.repository.MeetupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MeetupListState {
    object Idle : MeetupListState()
    object Loading : MeetupListState()
    data class Success(val meetups: List<Meetup>, val currentUserId: String?) : MeetupListState()
    data class Error(val message: String) : MeetupListState()
}

sealed class MeetupActionState {
    object Idle : MeetupActionState()
    object Loading : MeetupActionState()
    object Success : MeetupActionState()
    data class Error(val message: String) : MeetupActionState()
}

class MeetupViewModel : ViewModel() {

    private val repository = MeetupRepository()

    private val _meetupListState = MutableStateFlow<MeetupListState>(MeetupListState.Idle)
    val meetupListState: StateFlow<MeetupListState> = _meetupListState

    private val _meetupActionState = MutableStateFlow<MeetupActionState>(MeetupActionState.Idle)
    val meetupActionState: StateFlow<MeetupActionState> = _meetupActionState

    fun loadMeetups() {
        viewModelScope.launch {
            _meetupListState.value = MeetupListState.Loading
            val result = repository.getAllMeetups()
            _meetupListState.value = if (result.isSuccess) {
                MeetupListState.Success(
                    meetups = result.getOrNull()!!,
                    currentUserId = repository.currentUserId
                )
            } else {
                MeetupListState.Error(result.exceptionOrNull()?.message ?: "Failed to load meetups")
            }
        }
    }

    fun createMeetup(location: String, dateTime: Long, description: String, dogBreeds: List<String>) {
        viewModelScope.launch {
            _meetupActionState.value = MeetupActionState.Loading
            val result = repository.createMeetup(location, dateTime, description, dogBreeds)
            _meetupActionState.value = if (result.isSuccess) {
                MeetupActionState.Success
            } else {
                MeetupActionState.Error(result.exceptionOrNull()?.message ?: "Failed to create meetup")
            }
        }
    }

    fun joinMeetup(meetupId: String) {
        viewModelScope.launch {
            _meetupActionState.value = MeetupActionState.Loading
            val result = repository.joinMeetup(meetupId)
            if (result.isSuccess) {
                loadMeetups()
            } else {
                _meetupActionState.value = MeetupActionState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to join meetup"
                )
            }
        }
    }

    fun leaveMeetup(meetupId: String) {
        viewModelScope.launch {
            _meetupActionState.value = MeetupActionState.Loading
            val result = repository.leaveMeetup(meetupId)
            if (result.isSuccess) {
                loadMeetups()
            } else {
                _meetupActionState.value = MeetupActionState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to leave meetup"
                )
            }
        }
    }

    fun deleteMeetup(meetupId: String) {
        viewModelScope.launch {
            _meetupActionState.value = MeetupActionState.Loading
            val result = repository.deleteMeetup(meetupId)
            if (result.isSuccess) {
                loadMeetups()
            } else {
                _meetupActionState.value = MeetupActionState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to delete meetup"
                )
            }
        }
    }

    fun resetActionState() {
        _meetupActionState.value = MeetupActionState.Idle
    }
}
