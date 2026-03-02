package com.example.astracker.network.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

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

// ── Supabase Profile (maps directly to the profiles table) ────────────────────

@Serializable
data class SupabaseProfile(
    val id: String = "",
    val name: String? = null,
    val major: String? = null,
    val year: Int? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

// ── Assignments ───────────────────────────────────────────────────────────────

@Serializable
data class SubtaskDto(
    val title: String,
    val checked: Boolean = false
)

@Serializable
data class AssignmentDto(
    // Supabase uses UUID 'id', not MongoDB '_id'
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val subject: String = "",
    @SerialName("due_date")
    val dueDate: String = "",
    val priority: String = "Medium",   // "Low" | "Medium" | "High"
    val status: String = "pending",    // "pending" | "completed" | "overdue"
    val subtasks: List<SubtaskDto> = emptyList(),
    val progress: Int = 0,
    val tags: List<String> = emptyList(),
    val type: String = "Assignment",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = ""
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
    @SerialName("is_primary")
    val isPrimary: Boolean = false
)

@Serializable
data class NotificationDto(
    // Supabase uses UUID 'id'
    val id: String = "",
    val title: String = "",
    val body: String = "",
    // Supabase column: bold_word
    @SerialName("bold_word")
    val boldWord: String = "",
    val type: String = "general",
    // Supabase column: is_read
    @SerialName("is_read")
    val isRead: Boolean = false,
    val actions: List<ActionDto> = emptyList(),
    @SerialName("created_at")
    val createdAt: String = ""
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
    @SerialName("avatar_url")
    val avatarUrl: String? = null
)

// ── Generic ───────────────────────────────────────────────────────────────────

@Serializable
data class MessageResponse(
    val success: Boolean,
    val message: String
)
