package com.example.astracker.network.models

import kotlinx.serialization.Serializable

// ── Auth ──────────────────────────────────────────────────────────────────────

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val major: String = "",
    val year: Int = 1
)

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val major: String,
    val year: Int,
    @SerialName("avatar_url")
    val avatarUrl: String
)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val token: String,
    val user: UserDto
)

// ── Assignments ───────────────────────────────────────────────────────────────

@Serializable
data class SubtaskDto(
    val _id: String = "",
    val title: String,
    val checked: Boolean = false
)

@Serializable
data class AssignmentDto(
    val id: String,           // Changed from _id to id for Postgres
    val title: String,
    val description: String,
    val subject: String,
    @SerialName("due_date")
    val dueDate: String,
    val priority: String,      // "Low" | "Medium" | "High"
    val status: String,        // "pending" | "completed" | "overdue"
    val subtasks: List<SubtaskDto> = emptyList(),
    val progress: Int = 0,
    val tags: List<String> = emptyList(),
    val type: String = "Assignment",
    @SerialName("created_at")
    val createdAt: String = ""
)

@Serializable
data class CreateAssignmentRequest(
    val title: String,
    val description: String,
    val subject: String,
    @SerialName("due_date")
    val dueDate: String,
    val priority: String,
    val subtasks: List<SubtaskDto> = emptyList(),
    val tags: List<String> = emptyList(),
    val type: String = "Assignment"
)

@Serializable
data class AssignmentStatsDto(
    val total: Int,
    val completed: Int,
    val active: Int,
    val overdue: Int,
    val dueToday: Int,
    val avgProgress: Int
)

@Serializable
data class AssignmentListResponse(
    val success: Boolean,
    val count: Int,
    val data: List<AssignmentDto>
)

@Serializable
data class AssignmentResponse(
    val success: Boolean,
    val data: AssignmentDto
)

@Serializable
data class StatsResponse(
    val success: Boolean,
    val data: AssignmentStatsDto
)

// ── Notifications ─────────────────────────────────────────────────────────────

@Serializable
data class ActionDto(
    val label: String,
    val isPrimary: Boolean
)

@Serializable
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

@Serializable
data class NotificationGroupsDto(
    val today: List<NotificationDto>,
    val yesterday: List<NotificationDto>,
    val earlier: List<NotificationDto>
)

@Serializable
data class NotificationListResponse(
    val success: Boolean,
    val count: Int,
    val unreadCount: Int,
    val data: NotificationGroupsDto
)

@Serializable
data class NotificationResponse(
    val success: Boolean,
    val data: NotificationDto
)

// ── Profile ───────────────────────────────────────────────────────────────────

@Serializable
data class ProfileStatsDto(
    val totalAssignments: Int,
    val completed: Int,
    val active: Int,
    val overdue: Int,
    val avgProgress: Int
)

@Serializable
data class ProfileDataDto(
    val user: UserDto,
    val stats: ProfileStatsDto
)

@Serializable
data class ProfileResponse(
    val success: Boolean,
    val data: ProfileDataDto
)

@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val major: String? = null,
    val year: Int? = null,
    val avatarUrl: String? = null
)

// ── Generic ───────────────────────────────────────────────────────────────────

@Serializable
data class MessageResponse(
    val success: Boolean,
    val message: String
)
