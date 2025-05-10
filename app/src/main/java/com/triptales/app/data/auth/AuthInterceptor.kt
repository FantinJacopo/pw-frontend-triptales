package com.triptales.app.data.auth

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import android.util.Log
import com.triptales.app.data.auth.TokenManager

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        try {
            runBlocking {
                val token = tokenManager.accessToken.first()
                if (!token.isNullOrBlank()) {
                    Log.d("AuthInterceptor", "Token trovato: Bearer $token")
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                } else {
                    Log.w("AuthInterceptor", "Nessun token trovato o token vuoto")
                }
            }
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Errore durante il recupero del token: ${e.message}")
        }

        val request = requestBuilder.build()
        Log.d("AuthInterceptor", "Richiesta a: ${request.url}")
        return chain.proceed(request)
    }
}
