package com.triptales.app.data.group

import com.triptales.app.data.utils.toRequestBody
import okhttp3.MultipartBody

class TripGroupRepository(private val api: TripGroupApi) {
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
        val response = api.joinGroup(JoinGroupRequest(qrCode))
        if (response.isSuccessful) {
            return response.body()
        } else {
            throw Exception("Errore API: ${response.errorBody()?.string()}")
        }
    }

    // Metodo esplicito per ottenere i gruppi dell'utente
    suspend fun getUserGroups() = api.getUserGroups()
}