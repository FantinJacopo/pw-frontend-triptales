package com.triptales.app.data.auth

import android.util.Log
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Evita di aggiungere l'header auth alle richieste di login/register/refresh
        val isAuthRequest = originalRequest.url.pathSegments.any {
            it in listOf("register", "token", "refresh")
        }

        if (isAuthRequest) {
            return chain.proceed(originalRequest)
        }

        return runBlocking {
            var token = tokenManager.accessToken.first()

            // Se il token è scaduto, prova a rinnovarlo
            if (tokenManager.isTokenExpired(token)) {
                Log.d("AuthInterceptor", "Token scaduto, tentativo di refresh...")
                val refreshSuccess = tokenManager.refreshAccessToken()
                if (refreshSuccess) {
                    token = tokenManager.accessToken.first()
                    Log.d("AuthInterceptor", "Token refreshato con successo")
                } else {
                    Log.w("AuthInterceptor", "Impossibile rinnovare il token")
                    // Se non riesce a rinnovare, pulisce tutto
                    tokenManager.clearTokens()
                }
            }

            // Aggiunge l'header autorizzazione se il token è disponibile
            val requestBuilder = originalRequest.newBuilder()
            if (!token.isNullOrBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
                Log.d("AuthInterceptor", "Header aggiunto: Bearer $token")
            }

            val request = requestBuilder.build()
            val response = chain.proceed(request)

            // Se riceve 401, prova una volta a rinnovare il token
            if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED && !token.isNullOrBlank()) {
                Log.d("AuthInterceptor", "Ricevuto 401, tentativo di refresh del token...")
                response.close()

                val refreshSuccess = tokenManager.refreshAccessToken()
                if (refreshSuccess) {
                    val newToken = tokenManager.accessToken.first()
                    val newRequest = originalRequest.newBuilder()
                        .addHeader("Authorization", "Bearer $newToken")
                        .build()
                    Log.d("AuthInterceptor", "Token refreshato, nuovo tentativo con token: $newToken")
                    return@runBlocking chain.proceed(newRequest)
                } else {
                    Log.w("AuthInterceptor", "Refresh fallito, clearing tokens")
                    tokenManager.clearTokens()
                }
            }

            response
        }
    }
}