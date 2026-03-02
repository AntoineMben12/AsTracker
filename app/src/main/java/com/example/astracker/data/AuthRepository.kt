package com.example.astracker.data

import com.example.astracker.network.SupabaseClientConfig
import com.example.astracker.network.ApiClient
import com.example.astracker.network.models.AuthResponse
import com.example.astracker.network.models.LoginRequest
import com.example.astracker.network.models.RegisterRequest
import io.github.jan.supabase.auth.auth

class AuthRepository(private val tokenManager: TokenManager) {

    private val api get() = ApiClient.api

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

    /**
     * Signs the user out of Supabase (invalidates the server-side session)
     * and clears the locally stored token.
     */
    suspend fun logout() {
        try {
            SupabaseClientConfig.client.auth.signOut()
        } catch (_: Exception) {
            // Best-effort – proceed even if the network call fails
        }
        tokenManager.clearToken()
    }

    /**
     * Returns true when the Supabase SDK still holds a valid session
     * (survives process restarts when the SDK persists the session on-device).
     */
    fun isLoggedIn(): Boolean =
        SupabaseClientConfig.client.auth.currentSessionOrNull() != null

    private fun parseError(body: String?): String {
        if (body == null) return "Unknown error"
        return try {
            val obj = org.json.JSONObject(body)
            obj.optString("error", obj.optString("message", "Unknown error"))
        } catch (_: Exception) {
            body.take(120)
        }
    }
}
