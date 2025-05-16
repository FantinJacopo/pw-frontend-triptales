package com.triptales.app.data.group

import android.util.Log
import com.triptales.app.data.utils.toRequestBody
import com.google.gson.Gson
import okhttp3.MultipartBody

class TripGroupRepository(private val api: TripGroupApi) {
    private val gson = Gson()

    // Usa getUserGroups invece di getGroups per ottenere solo i gruppi dell'utente
    suspend fun getGroups() = api.getUserGroups()

    suspend fun createGroup(
        name: String,
        description: String,
        imagePart: MultipartBody.Part
    ) = api.createGroup(
        toRequestBody(name),
        toRequestBody(description),
        imagePart,
    )

    suspend fun joinGroup(qrCode: String): JoinGroupResponse? {
        return try {
            Log.d("TripGroupRepository", "Attempting to join group with code: $qrCode")

            val request = JoinGroupRequest(qrCode)
            Log.d("TripGroupRepository", "Request body: ${gson.toJson(request)}")

            val response = api.joinGroup(request)

            Log.d("TripGroupRepository", "Response code: ${response.code()}")
            Log.d("TripGroupRepository", "Response headers: ${response.headers()}")

            if (response.isSuccessful) {
                val body = response.body()
                Log.d("TripGroupRepository", "Join successful: ${body?.message}")
                body
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("TripGroupRepository", "Join failed: ${response.code()} - $errorBody")

                // Gestisci specifici codici di errore
                when (response.code()) {
                    400 -> throw Exception("Codice gruppo non valido")
                    404 -> throw Exception("Gruppo non trovato con questo codice")
                    405 -> throw Exception("Metodo non consentito - problema di configurazione server")
                    409 -> throw Exception("Sei già membro di questo gruppo")
                    else -> throw Exception("Errore durante l'unione al gruppo: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            Log.e("TripGroupRepository", "Exception in joinGroup", e)
            throw e
        }
    }

    // Metodo esplicito per ottenere i gruppi dell'utente
    suspend fun getUserGroups() = api.getUserGroups()
}