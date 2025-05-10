package com.triptales.app.data

import com.triptales.app.data.auth.AuthInterceptor
import com.triptales.app.data.auth.TokenManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitProvider {
    private const val IP_ADDRESS = "192.168.174.89"
    private const val PORT = 8000

    fun create(tokenManager: TokenManager): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .build()

        return Retrofit.Builder()
            .baseUrl("http:/$IP_ADDRESS:$PORT/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
