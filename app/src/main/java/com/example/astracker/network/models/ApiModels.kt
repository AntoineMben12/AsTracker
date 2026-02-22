package com.example.astracker.network.models

// ── Auth ──────────────────────────────────────────────────────────────────────

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val major: String = "",
    val year: Int = 1
)

data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val major: String,
    val year: Int,
    val avatarUrl: String
)

data class AuthResponse(
    val success: Boolean,
    val token: String,
    val user: UserDto
)

// ── Assignments ───────────────────────────────────────────────────────────────

data class SubtaskDto(
    val _id: String = "",
    val title: String,
    val checked: Boolean = false
)

data class AssignmentDto(
    val _id: String,
    val title: String,
    val description: String,
    val subject: String,
    val dueDate: String,
    val priority: String,      // "Low" | "Medium" | "High"
    val status: String,        // "pending" | "completed" | "overdue"
    val subtasks: List<SubtaskDto> = emptyList(),
    val progress: Int = 0,
    val tags: List<String> = emptyList(),
    val type: String = "Assignment",
    val createdAt: String = ""
)

data class CreateAssignmentRequest(
    val title: String,
    val description: String,
    val subject: String,
    val dueDate: String,
    val priority: String,
    val subtasks: List<SubtaskDto> = emptyList(),
    val tags: List<String> = emptyList(),
    val type: String = "Assignment"
)

data class AssignmentStatsDto(
    val total: Int,
    val completed: Int,
    val active: Int,
    val overdue: Int,
    val dueToday: Int,
    val avgProgress: Int
)

data class AssignmentListResponse(
    val success: Boolean,
    val count: Int,
    val data: List<AssignmentDto>
)

data class AssignmentResponse(
    val success: Boolean,
    val data: AssignmentDto
)

data class StatsResponse(
    val success: Boolean,
    val data: AssignmentStatsDto
)

// ── Notifications ─────────────────────────────────────────────────────────────

data class ActionDto(
    val label: String,
    val isPrimary: Boolean
)

data class NotificationDto(
    val _id: String,
    val title: String,
    val body: String,
    val boldWord: String,
    val type: String,
    val isRead: Boolean,
    val actions: List<ActionDto> = emptyList(),
    val createdAt: String
)

data class NotificationGroupsDto(
    val today: List<NotificationDto>,
    val yesterday: List<NotificationDto>,
    val earlier: List<NotificationDto>
)

data class NotificationListResponse(
    val success: Boolean,
    val count: Int,
    val unreadCount: Int,
    val data: NotificationGroupsDto
)

data class NotificationResponse(
    val success: Boolean,
    val data: NotificationDto
)

// ── Profile ───────────────────────────────────────────────────────────────────

data class ProfileStatsDto(
    val totalAssignments: Int,
    val completed: Int,
    val active: Int,
    val overdue: Int,
    val avgProgress: Int
)

data class ProfileDataDto(
    val user: UserDto,
    val stats: ProfileStatsDto
)

data class ProfileResponse(
    val success: Boolean,
    val data: ProfileDataDto
)

data class UpdateProfileRequest(
    val name: String? = null,
    val major: String? = null,
    val year: Int? = null,
    val avatarUrl: String? = null
)

// ── Generic ───────────────────────────────────────────────────────────────────

data class MessageResponse(
    val success: Boolean,
    val message: String
)
