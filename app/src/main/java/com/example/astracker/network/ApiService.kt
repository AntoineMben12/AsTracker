package com.example.astracker.network

import com.example.astracker.network.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getMe(): Response<ProfileResponse>

    // ── Assignments ───────────────────────────────────────────────────────────
    @GET("assignments")
    suspend fun getAssignments(
        @Query("status") status: String? = null,
        @Query("priority") priority: String? = null,
        @Query("subject") subject: String? = null
    ): Response<AssignmentListResponse>

    @GET("assignments/stats")
    suspend fun getAssignmentStats(): Response<StatsResponse>

    @GET("assignments/{id}")
    suspend fun getAssignment(@Path("id") id: String): Response<AssignmentResponse>

    @POST("assignments")
    suspend fun createAssignment(@Body request: CreateAssignmentRequest): Response<AssignmentResponse>

    @PUT("assignments/{id}")
    suspend fun updateAssignment(
        @Path("id") id: String,
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<AssignmentResponse>

    @DELETE("assignments/{id}")
    suspend fun deleteAssignment(@Path("id") id: String): Response<MessageResponse>

    // ── Notifications ─────────────────────────────────────────────────────────
    @GET("notifications")
    suspend fun getNotifications(): Response<NotificationListResponse>

    @POST("notifications")
    suspend fun createNotification(@Body request: CreateNotificationRequest): Response<NotificationResponse>

    @PUT("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): Response<NotificationResponse>

    @PUT("notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<MessageResponse>

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: String): Response<MessageResponse>

    // ── Profile ───────────────────────────────────────────────────────────────
    @GET("profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @PUT("profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ProfileResponse>
}
