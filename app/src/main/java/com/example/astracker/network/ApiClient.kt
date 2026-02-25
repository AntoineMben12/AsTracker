package com.example.astracker.network

import com.example.astracker.data.TokenManager

object ApiClient {

    private val supabaseApi = SupabaseApiService()

    fun init(tokenManager: TokenManager) {
        // No-op for Supabase as initialization is handled in SupabaseClientConfig
    }

    val api: ApiService get() = supabaseApi
}
