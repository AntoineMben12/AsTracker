package com.example.astracker.ui.assignment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.astracker.data.AssignmentRepository
import com.example.astracker.network.models.AssignmentDto
import com.example.astracker.network.models.AssignmentStatsDto
import com.example.astracker.network.models.CreateAssignmentRequest
import com.example.astracker.network.models.SubtaskDto
import com.example.astracker.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AssignmentViewModel(
    private val repo: AssignmentRepository = AssignmentRepository()
) : ViewModel() {

    private val _assignments = MutableStateFlow<UiState<List<AssignmentDto>>>(UiState.Idle)
    val assignments: StateFlow<UiState<List<AssignmentDto>>> = _assignments

    private val _stats = MutableStateFlow<AssignmentStatsDto?>(null)
    val stats: StateFlow<AssignmentStatsDto?> = _stats

    private val _createState = MutableStateFlow<UiState<AssignmentDto>>(UiState.Idle)
    val createState: StateFlow<UiState<AssignmentDto>> = _createState

    init { loadAll() }

    fun loadAll(status: String? = null, priority: String? = null) {
        viewModelScope.launch {
            _assignments.value = UiState.Loading
            val result = repo.getAll(status = status, priority = priority)
            _assignments.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to load assignments") }
            )

            // Also refresh stats
            repo.getStats().onSuccess { _stats.value = it }
        }
    }

    fun createAssignment(
        title: String,
        description: String,
        subject: String,
        dueDate: String,
        priority: String,
        subtasks: List<SubtaskDto> = emptyList(),
        type: String = "Assignment"
    ) {
        if (title.isBlank() || subject.isBlank() || dueDate.isBlank()) {
            _createState.value = UiState.Error("Title, subject and due date are required")
            return
        }
        viewModelScope.launch {
            _createState.value = UiState.Loading
            val result = repo.create(
                CreateAssignmentRequest(title, description, subject, dueDate, priority, subtasks, type = type)
            )
            _createState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to create assignment") }
            )
            // Refresh list after create
            if (result.isSuccess) loadAll()
        }
    }

    fun markComplete(id: String) {
        viewModelScope.launch {
            repo.update(id, mapOf("status" to "completed", "progress" to 100))
            loadAll()
        }
    }

    fun deleteAssignment(id: String) {
        viewModelScope.launch {
            repo.delete(id)
            loadAll()
        }
    }

    fun resetCreateState() { _createState.value = UiState.Idle }
}
