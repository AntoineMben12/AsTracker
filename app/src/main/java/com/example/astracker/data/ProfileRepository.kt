package com.example.astracker.data

import com.example.astracker.network.ApiClient
import com.example.astracker.network.models.ProfileDataDto
import com.example.astracker.network.models.UpdateProfileRequest
import org.json.JSONObject

class ProfileRepository {

    private val api get() = ApiClient.api

    suspend fun getProfile(): Result<ProfileDataDto> {
        return try {
            val response = api.getProfile()
            if (response.isSuccessful) Result.success(response.body()!!.data)
            else Result.failure(Exception(parseError(response.errorBody()?.string())))
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Result<ProfileDataDto> {
        return try {
            val response = api.updateProfile(request)
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
