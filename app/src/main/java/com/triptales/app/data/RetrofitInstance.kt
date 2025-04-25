package com.triptales.app.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val IP_ADDRESS = "192.168.1.72"
    private const val PORT = 8000
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://$IP_ADDRESS:$PORT/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }
}