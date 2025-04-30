package com.triptales.app.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val IP_ADDRESS = "172.17.5.150"
    private const val PORT = 8000
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://$IP_ADDRESS:$PORT/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val tripGroupApi: TripGroupApi by lazy {
        retrofit.create(TripGroupApi::class.java)
    }

    val api: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }
}