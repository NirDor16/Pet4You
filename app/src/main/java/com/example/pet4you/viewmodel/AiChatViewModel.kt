package com.example.pet4you.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pet4you.data.model.ChatMessage
import com.example.pet4you.repository.AiChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ChatState {
    object Idle : ChatState()
    object Loading : ChatState()
    data class Success(val reply: String) : ChatState()
    data class Error(val message: String) : ChatState()
}

class AiChatViewModel : ViewModel() {
    private val repository = AiChatRepository()

    private val _state = MutableStateFlow<ChatState>(ChatState.Idle)
    val state: StateFlow<ChatState> = _state

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    fun sendMessage(userMessage: String) {
        val historyBeforeThisMessage = _messages.value
        _messages.value = historyBeforeThisMessage + ChatMessage(role = "user", content = userMessage)
        _state.value = ChatState.Loading

        viewModelScope.launch {
            val result = repository.sendMessage(
                message = userMessage,
                history = historyBeforeThisMessage
            )
            if (result.isSuccess) {
                val reply = result.getOrNull()!!
                _messages.value = _messages.value + ChatMessage(role = "assistant", content = reply)
                _state.value = ChatState.Idle
            } else {
                _state.value = ChatState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun resetError() {
        _state.value = ChatState.Idle
    }
}
