package com.example.astracker.network

// Change jan_tennert to jan
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClientConfig {
    private const val SUPABASE_URL = "https://inrmqatnlfgysjjnifpk.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_MFhPEBYsyf4HawitS_UUqg_oSabIxs_" // From .env

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        // In v3.x, you often use the plugin objects directly
        install(Auth)
        install(Postgrest)
    }
}