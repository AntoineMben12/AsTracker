package com.example.astracker.data

import com.example.astracker.network.ApiClient
import com.example.astracker.network.models.CreateNotificationRequest
import com.example.astracker.network.models.NotificationDto
import com.example.astracker.network.models.NotificationGroupsDto
import org.json.JSONObject

class NotificationRepository {

    private val api get() = ApiClient.api

    suspend fun getAll(): Result<NotificationGroupsDto> {
        return try {
            val response = api.getNotifications()
            if (response.isSuccessful) Result.success(response.body()!!.data)
            else Result.failure(Exception(parseError(response.errorBody()?.string())))
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun markRead(id: String): Result<Unit> {
        return try {
            val response = api.markNotificationRead(id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(parseError(response.errorBody()?.string())))
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun markAllRead(): Result<Unit> {
        return try {
            val response = api.markAllNotificationsRead()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(parseError(response.errorBody()?.string())))
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun delete(id: String): Result<Unit> {
        return try {
            val response = api.deleteNotification(id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(parseError(response.errorBody()?.string())))
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun create(request: CreateNotificationRequest): Result<NotificationDto> {
        return try {
            val response = api.createNotification(request)
            if (response.isSuccessful) Result.success(response.body()!!.data)
            else Result.failure(Exception(parseError(response.errorBody()?.string())))
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    private fun parseError(body: String?): String {
        if (body == null) return "Unknown error"
        return try {
            JSONObject(body).optString("error", "Unknown error")
        } catch (_: Exception) { "Unknown error" }
    }
}
