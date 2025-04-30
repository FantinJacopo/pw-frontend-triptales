package com.triptales.app.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitProvider {

    fun create(tokenManager: TokenManager): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .build()

        return Retrofit.Builder()
            .baseUrl("http://172.17.5.150:8000/api/") // cambia con il tuo
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
