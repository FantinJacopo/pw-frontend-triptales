package com.triptales.app.data.utils

import android.util.Log
import retrofit2.Response

/**
 * Utility class for API response handling and error management.
 */
object ApiUtils {
    /**
     * Safely executes an API call and handles common error cases.
     *
     * @param tag The tag to use for logging
     * @param operation The API call operation description for logging
     * @param apiCall The suspend function that makes the API call
     * @return The API response
     * @throws Exception with a user-friendly error message
     */
    suspend fun <T> safeApiCall(
        tag: String,
        operation: String,
        apiCall: suspend () -> Response<T>
    ): Response<T> {
        return try {
            Log.d(tag, "Executing API call: $operation")
            val response = apiCall()

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e(tag, "API call failed: ${response.code()} - $errorBody")

                // Translate common status codes to user-friendly messages
                val errorMessage = when(response.code()) {
                    400 -> "Richiesta non valida. Verifica i dati inseriti."
                    401 -> "Sessione scaduta. Effettua nuovamente il login."
                    403 -> "Non hai i permessi necessari per questa operazione."
                    404 -> "Risorsa non trovata."
                    409 -> "Esiste già un record con queste informazioni."
                    429 -> "Troppe richieste. Riprova tra qualche istante."
                    500, 502, 503, 504 -> "Errore del server. Riprova più tardi."
                    else -> "Errore di comunicazione: ${response.code()}"
                }

                throw Exception(errorMessage)
            }

            Log.d(tag, "API call successful: $operation")
            response
        } catch (e: Exception) {
            Log.e(tag, "Exception during API call", e)
            throw e
        }
    }

    /**
     * Safely executes an API call with response body extraction and error handling.
     *
     * @param tag The tag to use for logging
     * @param operation The API call operation description for logging
     * @param apiCall The suspend function that makes the API call
     * @return The data from the API response body
     * @throws Exception with a user-friendly error message
     */
    suspend fun <T> safeApiCallWithBody(
        tag: String,
        operation: String,
        apiCall: suspend () -> Response<T>
    ): T {
        val response = safeApiCall(tag, operation, apiCall)

        return response.body() ?: throw Exception("Risposta vuota dal server")
    }

    /**
     * Map an exception to a user-friendly error message.
     *
     * @param e The exception to map
     * @param defaultMessage The default message to use if no specific mapping exists
     * @return A user-friendly error message
     */
    fun getErrorMessage(e: Exception, defaultMessage: String = "Si è verificato un errore"): String {
        return when {
            e.message?.contains("timeout") == true -> "Timeout della connessione. Verifica la tua rete."
            e.message?.contains("Unable to resolve host") == true -> "Impossibile connettersi al server. Verifica la tua connessione."
            e.message?.contains("401") == true -> "Sessione scaduta. Effettua nuovamente il login."
            e.message?.contains("403") == true -> "Non hai i permessi necessari."
            e.message?.contains("404") == true -> "La risorsa richiesta non è stata trovata."
            e.message?.contains("409") == true -> "Esiste già un record con questi dati."
            e.message?.contains("500") == true -> "Errore interno del server. Riprova più tardi."
            e.message?.contains("refuse") == true -> "Connessione rifiutata. Il server potrebbe non essere disponibile."
            else -> e.message ?: defaultMessage
        }
    }
}