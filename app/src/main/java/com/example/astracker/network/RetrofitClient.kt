package com.example.astracker.network

import com.example.astracker.data.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // 10.0.2.2 is the Android emulator alias for the host machine's localhost.
    // For a real device, replace with your machine's LAN IP: e.g. "http://192.168.1.x:5000/api/"
    private const val BASE_URL = "http://10.0.2.2:5000/api/"

    private var tokenManager: TokenManager? = null

    fun init(tokenManager: TokenManager) {
        RetrofitClient.tokenManager = tokenManager
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager!!))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
