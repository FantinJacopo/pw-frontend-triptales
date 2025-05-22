
package com.triptales.app.data.user

import android.util.Log
import com.triptales.app.data.utils.ApiUtils.safeApiCall

class UserRepository(private val api: UserApi) {
    companion object {
        private const val TAG = "UserRepository"
    }

    suspend fun getUserProfile() = api.getUserProfile()

    suspend fun getUserById(userId: Int) = api.getUserById(userId)

    suspend fun getUserBadges() = api.getUserBadges()

    suspend fun getUserBadges(userId: Int) = api.getUserBadges(userId)

    /**
     * Verifica e assegna badge mancanti all'utente corrente.
     * Utile per assegnare retroattivamente badge che potrebbero non essere stati
     * assegnati automaticamente dai signal Django.
     *
     * @return Response con i dettagli dei badge assegnati
     */
    suspend fun checkAndAssignBadges() = safeApiCall(
        tag = TAG,
        operation = "check and assign badges",
        apiCall = { api.checkAndAssignBadges() }
    )
}