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
import java.util.Calendar

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

    fun loadAll(status: String? = null, priority: String? = null, subject: String? = null) {
        viewModelScope.launch {
            _assignments.value = UiState.Loading
            val result = repo.getAll(status = status, priority = priority, subject = subject)
            _assignments.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to load assignments") }
            )

            // Compute stats locally — avoids a second network round-trip
            result.onSuccess { list ->
                _stats.value = computeStats(list)
            }
        }
    }

    fun createAssignment(
        title: String,
        description: String,
        subject: String,
        dueDate: String,        // Expected as ISO-8601 string, e.g. "2024-12-31T23:59:00+00:00"
        priority: String,
        subtasks: List<SubtaskDto> = emptyList(),
        tags: List<String> = emptyList(),
        type: String = "Assignment"
    ) {
        if (title.isBlank() || subject.isBlank() || dueDate.isBlank()) {
            _createState.value = UiState.Error("Title, subject and due date are required")
            return
        }
        viewModelScope.launch {
            _createState.value = UiState.Loading
            val result = repo.create(
                CreateAssignmentRequest(
                    title       = title.trim(),
                    description = description.trim(),
                    subject     = subject.trim(),
                    dueDate     = dueDate,
                    priority    = priority,
                    subtasks    = subtasks,
                    tags        = tags,
                    type        = type
                )
            )
            _createState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to create assignment") }
            )
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

    fun resetCreateState() {
        _createState.value = UiState.Idle
    }

    // ─────────────────────────── Helpers ──────────────────────────────────────

    private fun todayStr(): String {
        val c = Calendar.getInstance()
        return "%04d-%02d-%02d".format(
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH) + 1,
            c.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun computeStats(list: List<AssignmentDto>): AssignmentStatsDto {
        val today = todayStr()
        return AssignmentStatsDto(
            total       = list.size,
            completed   = list.count { it.status == "completed" },
            active      = list.count { it.status == "pending"   },
            overdue     = list.count { it.status == "overdue"   },
            dueToday    = list.count { it.dueDate.startsWith(today) && it.status != "completed" },
            avgProgress = if (list.isEmpty()) 0 else list.sumOf { it.progress } / list.size
        )
    }
}
