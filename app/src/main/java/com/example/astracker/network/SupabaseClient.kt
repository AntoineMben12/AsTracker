package com.example.astracker.network

import io.github.jan_tennert.supabase.SupabaseClient
import io.github.jan_tennert.supabase.auth.Auth
import io.github.jan_tennert.supabase.createSupabaseClient
import io.github.jan_tennert.supabase.postgrest.Postgrest

object SupabaseClientConfig {
    private const val SUPABASE_URL = "https://inrmqatnlfgysjjnifpk.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_MFhPEBYsyf4HawitS_UUqg_oSabIxs_" // From .env

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }
}
