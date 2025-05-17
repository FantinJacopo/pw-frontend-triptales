package com.triptales.app.data.group

import android.util.Log
import com.triptales.app.data.utils.toRequestBody
import com.google.gson.Gson
import com.triptales.app.data.utils.ApiUtils.getErrorMessage
import com.triptales.app.data.utils.ApiUtils.safeApiCall
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

            val response = safeApiCall(
                tag = "TripGroupRepository",
                operation = "join group with QR code",
                apiCall = { api.joinGroup(request) }
            )

            return response.body()
        } catch (e: Exception) {
            Log.e("TripGroupRepository", "Exception in joinGroup", e)
            val errorMessage = getErrorMessage(e, "Errore durante l'unione al gruppo")
            throw Exception(errorMessage)
        }
    }

    // Metodo esplicito per ottenere i gruppi dell'utente
    suspend fun getUserGroups() = api.getUserGroups()
}