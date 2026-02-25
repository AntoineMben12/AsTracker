package com.example.astracker.network

import com.example.astracker.network.models.*
import io.github.jan_tennert.supabase.auth.auth
import io.github.jan_tennert.supabase.postgrest.postgrest
import io.github.jan_tennert.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class SupabaseApiService : ApiService {

    private val auth = SupabaseClientConfig.client.auth
    private val db = SupabaseClientConfig.client.postgrest

    override suspend fun register(request: RegisterRequest): Response<AuthResponse> {
        return try {
            auth.signUpWith(io.github.jan_tennert.supabase.auth.providers.builtin.Email) {
                email = request.email
                password = request.password
                data = buildMap {
                    put("name", request.name)
                    put("major", request.major)
                    put("year", request.year)
                }
            }
            val session = auth.currentSessionOrNull()
            Response.success(AuthResponse(
                success = true,
                token = session?.accessToken ?: "",
                user = UserDto(
                    id = session?.user?.id ?: "",
                    name = request.name,
                    email = request.email,
                    major = request.major,
                    year = request.year,
                    avatarUrl = ""
                )
            ))
        } catch (e: Exception) {
            Response.error(400, okhttp3.ResponseBody.create(null, e.message ?: "Error"))
        }
    }

    override suspend fun login(request: LoginRequest): Response<AuthResponse> {
        return try {
            auth.signInWith(io.github.jan_tennert.supabase.auth.providers.builtin.Email) {
                email = request.email
                password = request.password
            }
            val session = auth.currentSessionOrNull()
            Response.success(AuthResponse(
                success = true,
                token = session?.accessToken ?: "",
                user = UserDto(
                    id = session?.user?.id ?: "",
                    name = session?.user?.userMetadata?.get("name").toString(),
                    email = request.email,
                    major = session?.user?.userMetadata?.get("major").toString(),
                    year = session?.user?.userMetadata?.get("year").toString().toIntOrNull() ?: 1,
                    avatarUrl = ""
                )
            ))
        } catch (e: Exception) {
            Response.error(401, okhttp3.ResponseBody.create(null, e.message ?: "Error"))
        }
    }

    override suspend fun getMe(): Response<ProfileResponse> {
        // Implementation for auth/me
        return Response.success(ProfileResponse(true, ProfileDataDto(UserDto("", "", "", "", 1, ""), ProfileStatsDto(0, 0, 0, 0, 0))))
    }

    override suspend fun getAssignments(status: String?, priority: String?, subject: String?): Response<AssignmentListResponse> {
        return try {
            val query = db.from("assignments").select()
            status?.let { query.eq("status", it) }
            priority?.let { query.eq("priority", it) }
            subject?.let { query.eq("subject", it) }
            
            val data = query.decodeList<AssignmentDto>()
            Response.success(AssignmentListResponse(true, data.size, data))
        } catch (e: Exception) {
            Response.error(500, okhttp3.ResponseBody.create(null, e.message ?: "Error"))
        }
    }

    override suspend fun getAssignmentStats(): Response<StatsResponse> {
        // Implementation for stats
        return Response.success(StatsResponse(true, AssignmentStatsDto(0, 0, 0, 0, 0, 0)))
    }

    override suspend fun getAssignment(id: String): Response<AssignmentResponse> {
        return try {
            val data = db.from("assignments").select {
                filter { eq("id", id) }
            }.decodeSingle<AssignmentDto>()
            Response.success(AssignmentResponse(true, data))
        } catch (e: Exception) {
            Response.error(404, okhttp3.ResponseBody.create(null, e.message ?: "Error"))
        }
    }

    override suspend fun createAssignment(request: CreateAssignmentRequest): Response<AssignmentResponse> {
        return try {
            val data = db.from("assignments").insert(request).decodeSingle<AssignmentDto>()
            Response.success(AssignmentResponse(true, data))
        } catch (e: Exception) {
            Response.error(400, okhttp3.ResponseBody.create(null, e.message ?: "Error"))
        }
    }

    override suspend fun updateAssignment(id: String, request: Map<String, Any>): Response<AssignmentResponse> {
        return try {
            val data = db.from("assignments").update(request) {
                filter { eq("id", id) }
            }.decodeSingle<AssignmentDto>()
            Response.success(AssignmentResponse(true, data))
        } catch (e: Exception) {
            Response.error(400, okhttp3.ResponseBody.create(null, e.message ?: "Error"))
        }
    }

    override suspend fun deleteAssignment(id: String): Response<MessageResponse> {
        return try {
            db.from("assignments").delete {
                filter { eq("id", id) }
            }
            Response.success(MessageResponse(true, "Deleted"))
        } catch (e: Exception) {
            Response.error(400, okhttp3.ResponseBody.create(null, e.message ?: "Error"))
        }
    }

    override suspend fun getNotifications(): Response<NotificationListResponse> {
        // Implementation for notifications
        return Response.success(NotificationListResponse(true, 0, 0, NotificationGroupsDto(emptyList(), emptyList(), emptyList())))
    }

    override suspend fun markNotificationRead(id: String): Response<NotificationResponse> {
        // Implementation
        return Response.error(400, okhttp3.ResponseBody.create(null, "Not implemented"))
    }

    override suspend fun markAllNotificationsRead(): Response<MessageResponse> {
         // Implementation
        return Response.success(MessageResponse(true, "All read"))
    }

    override suspend fun deleteNotification(id: String): Response<MessageResponse> {
         // Implementation
        return Response.success(MessageResponse(true, "Deleted"))
    }

    override suspend fun getProfile(): Response<ProfileResponse> {
        return getMe()
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): Response<ProfileResponse> {
        // Implementation
        return getMe()
    }
}
