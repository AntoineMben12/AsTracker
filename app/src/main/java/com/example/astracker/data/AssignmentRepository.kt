package com.example.astracker.data

import com.example.astracker.network.ApiClient
import com.example.astracker.network.models.*
import org.json.JSONObject

class AssignmentRepository {

    private val api get() = ApiClient.api

    suspend fun getAll(
        status: String? = null,
        priority: String? = null,
        subject: String? = null
    ): Result<List<AssignmentDto>> {
        return try {
            val response = api.getAssignments(status, priority, subject)
            if (response.isSuccessful) Result.success(response.body()!!.data)
            else Result.failure(Exception(parseError(response.errorBody()?.string())))
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun getStats(): Result<AssignmentStatsDto> {
        return try {
            val response = api.getAssignmentStats()
            if (response.isSuccessful) Result.success(response.body()!!.data)
            else Result.failure(Exception(parseError(response.errorBody()?.string())))
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun create(request: CreateAssignmentRequest): Result<AssignmentDto> {
        return try {
            val response = api.createAssignment(request)
            if (response.isSuccessful) Result.success(response.body()!!.data)
            else Result.failure(Exception(parseError(response.errorBody()?.string())))
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun update(id: String, fields: Map<String, Any>): Result<AssignmentDto> {
        return try {
            val response = api.updateAssignment(id, fields)
            if (response.isSuccessful) Result.success(response.body()!!.data)
            else Result.failure(Exception(parseError(response.errorBody()?.string())))
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun delete(id: String): Result<Unit> {
        return try {
            val response = api.deleteAssignment(id)
            if (response.isSuccessful) Result.success(Unit)
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
