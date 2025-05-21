package com.triptales.app.data

import com.google.gson.GsonBuilder
import com.triptales.app.data.auth.AuthInterceptor
import com.triptales.app.data.auth.TokenManager
import com.triptales.app.data.utils.StringListAdapter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Provider per le istanze di Retrofit.
 * Configura le impostazioni di base, inclusi adattatori personalizzati per la serializzazione.
 */
object RetrofitProvider {
    private const val IP_ADDRESS = "192.168.1.3"
    private const val PORT = 8000

    /**
     * Crea un'istanza Retrofit con interceptor per l'autenticazione.
     */
    fun create(tokenManager: TokenManager): Retrofit {
        // Logger per le richieste HTTP - aiuta molto con il debug
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Client HTTP con interceptor
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(loggingInterceptor)  // Aggiungi logging
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // Personalizza Gson per gestire la serializzazione/deserializzazione
        val gson = GsonBuilder()
            .registerTypeAdapter(List::class.java, StringListAdapter())
            .setLenient()  // Per essere più tollerante con JSON malformati
            .create()

        return Retrofit.Builder()
            .baseUrl("http://$IP_ADDRESS:$PORT/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Crea un'istanza Retrofit senza interceptor per l'autenticazione.
     * Usato solo per chiamate che non richiedono autenticazione (es. login, registrazione).
     */
    fun createUnauthenticated(): Retrofit {
        // Logger per le richieste HTTP
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Client HTTP base
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // Personalizza Gson
        val gson = GsonBuilder()
            .registerTypeAdapter(List::class.java, StringListAdapter())
            .setLenient()
            .create()

        return Retrofit.Builder()
            .baseUrl("http://$IP_ADDRESS:$PORT/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}