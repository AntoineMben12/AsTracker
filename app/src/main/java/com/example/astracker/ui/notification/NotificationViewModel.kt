package com.example.astracker.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.astracker.data.NotificationRepository
import com.example.astracker.network.models.NotificationGroupsDto
import com.example.astracker.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repo: NotificationRepository = NotificationRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<NotificationGroupsDto>>(UiState.Idle)
    val state: StateFlow<UiState<NotificationGroupsDto>> = _state

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val result = repo.getAll()
            result.onSuccess { groups ->
                _state.value = UiState.Success(groups)
                _unreadCount.value = groups.today.count { !it.isRead } +
                        groups.yesterday.count { !it.isRead } +
                        groups.earlier.count { !it.isRead }
            }
            result.onFailure { _state.value = UiState.Error(it.message ?: "Failed to load notifications") }
        }
    }

    fun markRead(id: String) {
        viewModelScope.launch {
            repo.markRead(id)
            load() // refresh
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            repo.markAllRead()
            load()
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            repo.delete(id)
            load()
        }
    }
}
