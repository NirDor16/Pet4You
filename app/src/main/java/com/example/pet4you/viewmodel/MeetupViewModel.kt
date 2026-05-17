package com.example.pet4you.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet4you.data.model.Meetup
import com.example.pet4you.network.ApiClient
import com.example.pet4you.network.RecommendMeetupsRequest
import com.example.pet4you.repository.DogRepository
import com.example.pet4you.repository.MeetupRepository
import com.google.firebase.auth.FirebaseAuth
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

sealed class RecommendState {
    object Idle : RecommendState()
    object Loading : RecommendState()
    data class Success(val meetups: List<Meetup>, val currentUserId: String?) : RecommendState()
    data class Error(val message: String) : RecommendState()
}

sealed class MeetupDetailState {
    object Idle : MeetupDetailState()
    object Loading : MeetupDetailState()
    data class Success(val meetup: Meetup, val currentUserId: String?) : MeetupDetailState()
    data class Error(val message: String) : MeetupDetailState()
}

class MeetupViewModel : ViewModel() {

    private val repository    = MeetupRepository()
    private val dogRepository = DogRepository()

    private val _meetupListState = MutableStateFlow<MeetupListState>(MeetupListState.Idle)
    val meetupListState: StateFlow<MeetupListState> = _meetupListState

    private val _meetupActionState = MutableStateFlow<MeetupActionState>(MeetupActionState.Idle)
    val meetupActionState: StateFlow<MeetupActionState> = _meetupActionState

    private val _recommendState = MutableStateFlow<RecommendState>(RecommendState.Idle)
    val recommendState: StateFlow<RecommendState> = _recommendState

    private val _detailState = MutableStateFlow<MeetupDetailState>(MeetupDetailState.Idle)
    val detailState: StateFlow<MeetupDetailState> = _detailState

    fun loadMeetups() {
        viewModelScope.launch {
            _meetupListState.value = MeetupListState.Loading
            refreshMeetupList()
        }
    }

    private suspend fun refreshMeetupList() {
        val result = repository.getAllMeetups()
        _meetupListState.value = if (result.isSuccess) {
            MeetupListState.Success(result.getOrNull()!!, repository.currentUserId)
        } else {
            MeetupListState.Error(result.exceptionOrNull()?.message ?: "Failed to load meetups")
        }
    }

    fun loadMeetupById(meetupId: String) {
        viewModelScope.launch {
            _detailState.value = MeetupDetailState.Loading
            val result = repository.getMeetupById(meetupId)
            _detailState.value = if (result.isSuccess) {
                MeetupDetailState.Success(result.getOrNull()!!, repository.currentUserId)
            } else {
                MeetupDetailState.Error(result.exceptionOrNull()?.message ?: "Meetup not found")
            }
        }
    }

    fun loadRecommendations() {
        viewModelScope.launch {
            _recommendState.value = RecommendState.Loading
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val breeds = dogRepository.getDogsForCurrentUser()
                    .getOrNull()
                    ?.map { it.breed }
                    ?.filter { it.isNotBlank() }
                    ?: emptyList()
                val meetups = repository.getAllMeetups().getOrNull() ?: emptyList()
                val response = ApiClient.apiService.recommendMeetups(
                    RecommendMeetupsRequest(dogBreeds = breeds, userId = uid, meetups = meetups)
                )
                _recommendState.value = RecommendState.Success(
                    meetups       = response.recommendations,
                    currentUserId = uid,
                )
            } catch (e: Exception) {
                _recommendState.value = RecommendState.Error(e.message ?: "Could not load recommendations")
            }
        }
    }

    fun createMeetup(
        title: String,
        location: String,
        dateTime: Long,
        description: String,
        dogBreeds: List<String>,
        participantLimit: Int,
    ) {
        viewModelScope.launch {
            _meetupActionState.value = MeetupActionState.Loading
            val result = repository.createMeetup(title, location, dateTime, description, dogBreeds, participantLimit)
            if (result.isSuccess) {
                _meetupActionState.value = MeetupActionState.Success
                launch { refreshMeetupList() }
            } else {
                _meetupActionState.value = MeetupActionState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to create meetup"
                )
            }
        }
    }

    // Called from the list screen — reloads full list silently on success
    fun joinMeetup(meetupId: String) {
        viewModelScope.launch {
            _meetupActionState.value = MeetupActionState.Loading
            val result = repository.joinMeetup(meetupId)
            if (result.isSuccess) refreshMeetupList()
            else _meetupActionState.value = MeetupActionState.Error(
                result.exceptionOrNull()?.message ?: "Failed to join meetup"
            )
        }
    }

    fun leaveMeetup(meetupId: String) {
        viewModelScope.launch {
            _meetupActionState.value = MeetupActionState.Loading
            val result = repository.leaveMeetup(meetupId)
            if (result.isSuccess) refreshMeetupList()
            else _meetupActionState.value = MeetupActionState.Error(
                result.exceptionOrNull()?.message ?: "Failed to leave meetup"
            )
        }
    }

    fun deleteMeetup(meetupId: String) {
        viewModelScope.launch {
            _meetupActionState.value = MeetupActionState.Loading
            val result = repository.deleteMeetup(meetupId)
            if (result.isSuccess) refreshMeetupList()
            else _meetupActionState.value = MeetupActionState.Error(
                result.exceptionOrNull()?.message ?: "Failed to delete meetup"
            )
        }
    }

    // Called from the detail screen — sets Success for the detail screen to react to,
    // and refreshes the list in the background so it's current when the user navigates back.
    fun joinMeetupFromDetail(meetupId: String) {
        viewModelScope.launch {
            _meetupActionState.value = MeetupActionState.Loading
            val result = repository.joinMeetup(meetupId)
            if (result.isSuccess) {
                _meetupActionState.value = MeetupActionState.Success
                launch { refreshMeetupList() }
            } else {
                _meetupActionState.value = MeetupActionState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to join meetup"
                )
            }
        }
    }

    fun leaveMeetupFromDetail(meetupId: String) {
        viewModelScope.launch {
            _meetupActionState.value = MeetupActionState.Loading
            val result = repository.leaveMeetup(meetupId)
            if (result.isSuccess) {
                _meetupActionState.value = MeetupActionState.Success
                launch { refreshMeetupList() }
            } else {
                _meetupActionState.value = MeetupActionState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to leave meetup"
                )
            }
        }
    }

    fun deleteMeetupFromDetail(meetupId: String) {
        viewModelScope.launch {
            _meetupActionState.value = MeetupActionState.Loading
            val result = repository.deleteMeetup(meetupId)
            if (result.isSuccess) {
                _meetupActionState.value = MeetupActionState.Success
                launch { refreshMeetupList() }
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
