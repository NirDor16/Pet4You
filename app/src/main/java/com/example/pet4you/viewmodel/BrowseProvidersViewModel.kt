package com.example.pet4you.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet4you.data.model.ServiceProvider
import com.example.pet4you.repository.ServiceProviderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class BrowseProvidersState {
    object Idle : BrowseProvidersState()
    object Loading : BrowseProvidersState()
    data class Success(
        val providers: List<ServiceProvider>,
        val activeFilter: String?
    ) : BrowseProvidersState()
    data class Error(val message: String) : BrowseProvidersState()
}

class BrowseProvidersViewModel : ViewModel() {

    private val repository = ServiceProviderRepository()

    private val _state = MutableStateFlow<BrowseProvidersState>(BrowseProvidersState.Idle)
    val state: StateFlow<BrowseProvidersState> = _state

    fun loadProviders(filter: String? = null) {
        viewModelScope.launch {
            _state.value = BrowseProvidersState.Loading
            val result = repository.getAllProviders(filter)
            _state.value = if (result.isSuccess) {
                BrowseProvidersState.Success(
                    providers = result.getOrNull()!!,
                    activeFilter = filter
                )
            } else {
                BrowseProvidersState.Error(result.exceptionOrNull()?.message ?: "Failed to load providers")
            }
        }
    }
}
