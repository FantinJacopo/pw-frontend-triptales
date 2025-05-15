package com.triptales.app.data.auth

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.triptales.app.data.RetrofitProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "user_prefs")
private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")

class TokenManager(private val context: Context) {

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    }

    // Mutex per evitare refresh concorrenti
    private val refreshMutex = Mutex()
    private var isRefreshing = false

    val accessToken: Flow<String?> = context.dataStore.data
        .map { prefs ->
            prefs[ACCESS_TOKEN_KEY]
        }

    val refreshToken: Flow<String?> = context.dataStore.data
        .map { prefs ->
            prefs[REFRESH_TOKEN_KEY]
        }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        Log.d("TokenManager", "Saving tokens...")
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN_KEY)
        }
    }

    suspend fun clearTokens() {
        Log.d("TokenManager", "Clearing all tokens...")
        context.dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN_KEY)
            prefs.remove(REFRESH_TOKEN_KEY)
        }
    }

    fun isTokenExpired(token: String?): Boolean {
        if (token.isNullOrBlank()) return true

        return try {
            val parts = token.split(".")
            if (parts.size != 3) return true

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val json = JSONObject(payload)

            val exp = json.getLong("exp")
            val now = System.currentTimeMillis() / 1000

            // Aggiunge un buffer di 5 minuti prima della scadenza
            val isExpired = exp < (now + 300)

            if (isExpired) {
                Log.d("TokenManager", "Token expired: exp=$exp, now=$now")
            }

            isExpired
        } catch (e: Exception) {
            Log.e("TokenManager", "Error checking token expiration", e)
            true
        }
    }

    fun clearIfExpired() {
        runBlocking {
            val access = accessToken.first()
            val refresh = refreshToken.first()
            if (isTokenExpired(access) && isTokenExpired(refresh)) {
                clearTokens()
            }
        }
    }

    suspend fun refreshAccessToken(): Boolean = refreshMutex.withLock {
        if (isRefreshing) {
            // Se è già in corso un refresh, aspetta che finisca
            return@withLock waitForRefresh()
        }

        isRefreshing = true

        try {
            val refresh = refreshToken.first()
            if (refresh.isNullOrBlank()) {
                Log.w("TokenManager", "No refresh token available")
                return@withLock false
            }

            // Controlla se il refresh token è scaduto
            if (isTokenExpired(refresh)) {
                Log.w("TokenManager", "Refresh token is expired")
                clearTokens()
                return@withLock false
            }

            val retrofit = RetrofitProvider.createUnauthenticated()
            val authApi = retrofit.create(AuthApi::class.java)

            Log.d("TokenManager", "Attempting to refresh token...")
            val response = authApi.refreshToken(RefreshRequest(refresh))

            if (response.isSuccessful && response.body() != null) {
                val newTokens = response.body()!!
                saveTokens(newTokens.access, refresh)
                Log.d("TokenManager", "Token refreshed successfully")
                return@withLock true
            } else {
                Log.e("TokenManager", "Token refresh failed: ${response.code()}")
                clearTokens()
                return@withLock false
            }
        } catch (e: Exception) {
            Log.e("TokenManager", "Error during token refresh", e)
            clearTokens()
            return@withLock false
        } finally {
            isRefreshing = false
        }
    }

    private suspend fun waitForRefresh(): Boolean {
        // Aspetta un massimo di 10 secondi per il refresh
        var attempts = 0
        while (isRefreshing && attempts < 100) {
            kotlinx.coroutines.delay(100)
            attempts++
        }

        // Controlla se il token è stato effettivamente rinnovato
        val token = accessToken.first()
        return !token.isNullOrBlank() && !isTokenExpired(token)
    }
}