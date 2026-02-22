package com.example.astracker

import android.app.Application
import com.example.astracker.data.TokenManager
import com.example.astracker.network.RetrofitClient

class AsTrackerApp : Application() {
    lateinit var tokenManager: TokenManager
        private set

    override fun onCreate() {
        super.onCreate()
        tokenManager = TokenManager(this)
        RetrofitClient.init(tokenManager)
    }
}
