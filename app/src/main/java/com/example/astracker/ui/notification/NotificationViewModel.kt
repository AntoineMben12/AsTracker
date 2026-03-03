package com.example.astracker.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.astracker.data.NotificationRepository
import com.example.astracker.network.models.ActionDto
import com.example.astracker.network.models.CreateNotificationRequest
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

    // ── Full load — shows spinner (used on first open) ─────────────────────────
    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            refresh()
        }
    }

    // ── Silent refresh — keeps existing data visible while fetching ────────────
    private suspend fun refresh() {
        val result = repo.getAll()
        result.onSuccess { groups ->
            _state.value = UiState.Success(groups)
            _unreadCount.value =
                groups.today.count     { !it.isRead } +
                groups.yesterday.count { !it.isRead } +
                groups.earlier.count   { !it.isRead }
        }
        result.onFailure { e ->
            // Only overwrite with error if we have nothing to show yet
            if (_state.value !is UiState.Success) {
                _state.value = UiState.Error(e.message ?: "Failed to load notifications")
            }
        }
    }

    // ── Mark single notification read ──────────────────────────────────────────
    fun markRead(id: String) {
        if (id.isBlank()) return
        viewModelScope.launch {
            repo.markRead(id)
            refresh()   // silent — list stays visible while updating
        }
    }

    // ── Mark all notifications read ────────────────────────────────────────────
    fun markAllRead() {
        viewModelScope.launch {
            repo.markAllRead()
            refresh()
        }
    }

    // ── Delete a notification ──────────────────────────────────────────────────
    fun delete(id: String) {
        if (id.isBlank()) return
        viewModelScope.launch {
            repo.delete(id)
            refresh()
        }
    }

    // ── Push: New Assignment notification ─────────────────────────────────────
    /**
     * Call this immediately after a successful createAssignment().
     *
     * @param assignmentTitle  The title of the newly created assignment.
     * @param subject          The subject / course name.
     * @param dueDate          Human-readable due date, e.g. "Dec 31, 2025".
     */
    fun notifyNewAssignment(
        assignmentTitle : String,
        subject         : String,
        dueDate         : String
    ) {
        viewModelScope.launch {
            repo.create(
                CreateNotificationRequest(
                    title    = "New Assignment Added",
                    body     = "\"$assignmentTitle\" for $subject is due on $dueDate.",
                    boldWord = assignmentTitle,
                    type     = "new_assignment",
                    actions  = listOf(
                        ActionDto(label = "View", isPrimary = true)
                    )
                )
            )
            refresh()   // pull the newly inserted row into the list
        }
    }

    // ── Push: Assignment Completed notification ────────────────────────────────
    /**
     * Call this immediately after a successful markComplete().
     *
     * @param assignmentTitle  The title of the completed assignment.
     * @param subject          The subject / course name.
     */
    fun notifyCompleted(
        assignmentTitle : String,
        subject         : String
    ) {
        viewModelScope.launch {
            repo.create(
                CreateNotificationRequest(
                    title    = "Assignment Completed \uD83C\uDF89",
                    body     = "Well done! \"$assignmentTitle\" for $subject has been marked as complete.",
                    boldWord = assignmentTitle,
                    type     = "completed"
                )
            )
            refresh()
        }
    }
}
