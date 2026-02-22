package com.example.astracker.data

import com.example.astracker.network.RetrofitClient
import com.example.astracker.network.models.AuthResponse
import com.example.astracker.network.models.LoginRequest
import com.example.astracker.network.models.RegisterRequest

class AuthRepository(private val tokenManager: TokenManager) {

    private val api get() = RetrofitClient.api

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                tokenManager.saveToken(body.token)
                Result.success(body)
            } else {
                Result.failure(Exception(parseError(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun register(
        name: String, email: String, password: String,
        major: String = "", year: Int = 1
    ): Result<AuthResponse> {
        return try {
            val response = api.register(RegisterRequest(name, email, password, major, year))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                tokenManager.saveToken(body.token)
                Result.success(body)
            } else {
                Result.failure(Exception(parseError(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    suspend fun logout() {
        tokenManager.clearToken()
    }

    private fun parseError(body: String?): String {
        if (body == null) return "Unknown error"
        return try {
            val obj = org.json.JSONObject(body)
            obj.optString("error", "Unknown error")
        } catch (_: Exception) {
            "Unknown error"
        }
    }
}
