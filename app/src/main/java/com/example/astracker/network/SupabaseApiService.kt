package com.example.astracker.network

import com.example.astracker.network.models.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonObject
import retrofit2.Response

class SupabaseApiService : ApiService {

    private val auth = SupabaseClientConfig.client.auth
    private val db   = SupabaseClientConfig.client.postgrest

    // ─────────────────────────── Date helpers ────────────────────────────────

    private fun todayStr(): String {
        val c = java.util.Calendar.getInstance()
        return "%04d-%02d-%02d".format(
            c.get(java.util.Calendar.YEAR),
            c.get(java.util.Calendar.MONTH) + 1,
            c.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    private fun yesterdayStr(): String {
        val c = java.util.Calendar.getInstance()
        c.add(java.util.Calendar.DAY_OF_YEAR, -1)
        return "%04d-%02d-%02d".format(
            c.get(java.util.Calendar.YEAR),
            c.get(java.util.Calendar.MONTH) + 1,
            c.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    private fun errBody(msg: String): okhttp3.ResponseBody =
        okhttp3.ResponseBody.create(null, msg)

    // ─────────────────────────── Stats builders ───────────────────────────────

    private fun buildAssignmentStats(list: List<AssignmentDto>): AssignmentStatsDto {
        val td = todayStr()
        return AssignmentStatsDto(
            total       = list.size,
            completed   = list.count { it.status == "completed" },
            active      = list.count { it.status == "pending"   },
            overdue     = list.count { it.status == "overdue"   },
            dueToday    = list.count { it.dueDate.startsWith(td) && it.status != "completed" },
            avgProgress = if (list.isEmpty()) 0
                          else list.sumOf { it.progress } / list.size
        )
    }

    private fun buildProfileStats(list: List<AssignmentDto>): ProfileStatsDto =
        ProfileStatsDto(
            totalAssignments = list.size,
            completed        = list.count { it.status == "completed" },
            active           = list.count { it.status == "pending"   },
            overdue          = list.count { it.status == "overdue"   },
            avgProgress      = if (list.isEmpty()) 0
                               else list.sumOf { it.progress } / list.size
        )

    // ───────────────────────────── Auth ──────────────────────────────────────

    override suspend fun register(request: RegisterRequest): Response<AuthResponse> {
        return try {
            auth.signUpWith(Email) {
                email    = request.email
                password = request.password
                data     = buildJsonObject {
                    put("name",  request.name)
                    put("major", request.major)
                    put("year",  request.year)
                }
            }
            val session = auth.currentSessionOrNull()
            Response.success(
                AuthResponse(
                    success = true,
                    token   = session?.accessToken ?: "",
                    user    = UserDto(
                        id        = session?.user?.id ?: "",
                        name      = request.name,
                        email     = request.email,
                        major     = request.major,
                        year      = request.year,
                        avatarUrl = ""
                    )
                )
            )
        } catch (e: Exception) {
            Response.error(400, errBody(e.message ?: "Registration failed"))
        }
    }

    override suspend fun login(request: LoginRequest): Response<AuthResponse> {
        return try {
            auth.signInWith(Email) {
                email    = request.email
                password = request.password
            }
            val session = auth.currentSessionOrNull()
            val meta    = session?.user?.userMetadata
            Response.success(
                AuthResponse(
                    success = true,
                    token   = session?.accessToken ?: "",
                    user    = UserDto(
                        id        = session?.user?.id ?: "",
                        name      = meta?.get("name") ?.toString()?.trim('"') ?: "",
                        email     = request.email,
                        major     = meta?.get("major")?.toString()?.trim('"') ?: "",
                        year      = meta?.get("year") ?.toString()?.trim('"')
                                        ?.toIntOrNull() ?: 1,
                        avatarUrl = ""
                    )
                )
            )
        } catch (e: Exception) {
            Response.error(401, errBody(e.message ?: "Login failed"))
        }
    }

    // ────────────────────────── Profile / Me ─────────────────────────────────

    override suspend fun getMe(): Response<ProfileResponse> {
        return try {
            val user = auth.currentUserOrNull()
                ?: return Response.error(401, errBody("Not authenticated"))

            // Fetch profile row
            val profile: SupabaseProfile = try {
                db.from("profiles")
                    .select { filter { eq("id", user.id) } }
                    .decodeSingle()
            } catch (_: Exception) {
                SupabaseProfile(id = user.id)
            }

            // Fetch all assignments for stats
            val assignments: List<AssignmentDto> = try {
                db.from("assignments").select().decodeList()
            } catch (_: Exception) {
                emptyList()
            }

            // Fallback to auth metadata if profile fields are null
            val meta  = user.userMetadata
            val name  = profile.name
                ?: meta?.get("name") ?.toString()?.trim('"') ?: ""
            val major = profile.major
                ?: meta?.get("major")?.toString()?.trim('"') ?: ""
            val year  = profile.year
                ?: meta?.get("year") ?.toString()?.trim('"')?.toIntOrNull() ?: 1

            Response.success(
                ProfileResponse(
                    success = true,
                    data    = ProfileDataDto(
                        user  = UserDto(
                            id        = user.id,
                            name      = name,
                            email     = user.email ?: "",
                            major     = major,
                            year      = year,
                            avatarUrl = profile.avatarUrl ?: ""
                        ),
                        stats = buildProfileStats(assignments)
                    )
                )
            )
        } catch (e: Exception) {
            Response.error(500, errBody(e.message ?: "Failed to load profile"))
        }
    }

    override suspend fun getProfile(): Response<ProfileResponse> = getMe()

    override suspend fun updateProfile(request: UpdateProfileRequest): Response<ProfileResponse> {
        return try {
            val user = auth.currentUserOrNull()
                ?: return Response.error(401, errBody("Not authenticated"))

            val json = buildJsonObject {
                request.name?.let      { put("name",       it) }
                request.major?.let     { put("major",      it) }
                request.year?.let      { put("year",       it) }
                request.avatarUrl?.let { put("avatar_url", it) }
                put("updated_at", java.util.Date().toString())
            }

            db.from("profiles").update(json) {
                filter { eq("id", user.id) }
            }

            // Return refreshed profile
            getMe()
        } catch (e: Exception) {
            Response.error(400, errBody(e.message ?: "Failed to update profile"))
        }
    }

    // ──────────────────────────── Assignments ─────────────────────────────────

    override suspend fun getAssignments(
        status: String?,
        priority: String?,
        subject: String?
    ): Response<AssignmentListResponse> {
        return try {
            val result = db.from("assignments").select {
                filter {
                    status?.let   { eq("status",   it) }
                    priority?.let { eq("priority", it) }
                    subject?.let  { eq("subject",  it) }
                }
                order("created_at", ascending = false)
            }
            val data = result.decodeList<AssignmentDto>()
            Response.success(AssignmentListResponse(true, data.size, data))
        } catch (e: Exception) {
            Response.error(500, errBody(e.message ?: "Failed to load assignments"))
        }
    }

    override suspend fun getAssignmentStats(): Response<StatsResponse> {
        return try {
            val list = db.from("assignments").select().decodeList<AssignmentDto>()
            Response.success(StatsResponse(true, buildAssignmentStats(list)))
        } catch (e: Exception) {
            Response.error(500, errBody(e.message ?: "Failed to load stats"))
        }
    }

    override suspend fun getAssignment(id: String): Response<AssignmentResponse> {
        return try {
            val data = db.from("assignments")
                .select { filter { eq("id", id) } }
                .decodeSingle<AssignmentDto>()
            Response.success(AssignmentResponse(true, data))
        } catch (e: Exception) {
            Response.error(404, errBody(e.message ?: "Assignment not found"))
        }
    }

    override suspend fun createAssignment(request: CreateAssignmentRequest): Response<AssignmentResponse> {
        return try {
            val data = db.from("assignments")
                .insert(request) { select() }
                .decodeSingle<AssignmentDto>()
            Response.success(AssignmentResponse(true, data))
        } catch (e: Exception) {
            Response.error(400, errBody(e.message ?: "Failed to create assignment"))
        }
    }

    override suspend fun updateAssignment(
        id: String,
        request: Map<String, @JvmSuppressWildcards Any>
    ): Response<AssignmentResponse> {
        return try {
            // Convert Map<String, Any> → JsonObject for the Supabase SDK
            val json: JsonObject = buildJsonObject {
                request.forEach { (key, value) ->
                    when (value) {
                        is String  -> put(key, value)
                        is Int     -> put(key, value)
                        is Long    -> put(key, value)
                        is Double  -> put(key, value)
                        is Float   -> put(key, value.toDouble())
                        is Boolean -> put(key, value)
                        else       -> put(key, value.toString())
                    }
                }
            }
            val data = db.from("assignments")
                .update(json) {
                    filter { eq("id", id) }
                    select()
                }
                .decodeSingle<AssignmentDto>()
            Response.success(AssignmentResponse(true, data))
        } catch (e: Exception) {
            Response.error(400, errBody(e.message ?: "Failed to update assignment"))
        }
    }

    override suspend fun deleteAssignment(id: String): Response<MessageResponse> {
        return try {
            db.from("assignments").delete { filter { eq("id", id) } }
            Response.success(MessageResponse(true, "Assignment deleted"))
        } catch (e: Exception) {
            Response.error(400, errBody(e.message ?: "Failed to delete assignment"))
        }
    }

    // ─────────────────────────── Notifications ────────────────────────────────

    override suspend fun getNotifications(): Response<NotificationListResponse> {
        return try {
            val all = db.from("notifications")
                .select { order("created_at", ascending = false) }
                .decodeList<NotificationDto>()

            val today     = todayStr()
            val yesterday = yesterdayStr()

            val todayList     = all.filter { it.createdAt.startsWith(today)     }
            val yesterdayList = all.filter { it.createdAt.startsWith(yesterday) }
            val earlierList   = all.filter {
                !it.createdAt.startsWith(today) && !it.createdAt.startsWith(yesterday)
            }

            val unread = all.count { !it.isRead }

            Response.success(
                NotificationListResponse(
                    success     = true,
                    count       = all.size,
                    unreadCount = unread,
                    data        = NotificationGroupsDto(
                        today     = todayList,
                        yesterday = yesterdayList,
                        earlier   = earlierList
                    )
                )
            )
        } catch (e: Exception) {
            Response.error(500, errBody(e.message ?: "Failed to load notifications"))
        }
    }

    override suspend fun markNotificationRead(id: String): Response<NotificationResponse> {
        return try {
            val json = buildJsonObject { put("is_read", true) }
            val data = db.from("notifications")
                .update(json) {
                    filter { eq("id", id) }
                    select()
                }
                .decodeSingle<NotificationDto>()
            Response.success(NotificationResponse(true, data))
        } catch (e: Exception) {
            Response.error(400, errBody(e.message ?: "Failed to mark notification read"))
        }
    }

    override suspend fun markAllNotificationsRead(): Response<MessageResponse> {
        return try {
            val user = auth.currentUserOrNull()
                ?: return Response.error(401, errBody("Not authenticated"))
            val json = buildJsonObject { put("is_read", true) }
            db.from("notifications").update(json) {
                filter {
                    eq("user_id", user.id)
                    eq("is_read", false)
                }
            }
            Response.success(MessageResponse(true, "All notifications marked as read"))
        } catch (e: Exception) {
            Response.error(400, errBody(e.message ?: "Failed to mark all notifications read"))
        }
    }

    override suspend fun deleteNotification(id: String): Response<MessageResponse> {
        return try {
            db.from("notifications").delete { filter { eq("id", id) } }
            Response.success(MessageResponse(true, "Notification deleted"))
        } catch (e: Exception) {
            Response.error(400, errBody(e.message ?: "Failed to delete notification"))
        }
    }
}
