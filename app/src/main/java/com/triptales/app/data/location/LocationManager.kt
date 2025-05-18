package com.triptales.app.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Manager per la gestione della geolocalizzazione dell'utente.
 * Utilizza Google Play Services per ottenere la posizione corrente.
 */
class LocationManager(private val context: Context) {

    companion object {
        private const val TAG = "LocationManager"
        private const val LOCATION_TIMEOUT = 10000L // 10 secondi

        /**
         * Calcola la distanza tra due punti in metri.
         */
        fun calculateDistance(from: LatLng, to: LatLng): Float {
            val results = FloatArray(1)
            Location.distanceBetween(
                from.latitude, from.longitude,
                to.latitude, to.longitude,
                results
            )
            return results[0]
        }

        /**
         * Formatta una distanza in un formato leggibile.
         */
        fun formatDistance(distanceInMeters: Float): String {
            return when {
                distanceInMeters < 1000 -> "${distanceInMeters.toInt()} m"
                else -> "%.1f km".format(distanceInMeters / 1000)
            }
        }
    }

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    /**
     * Controlla se i permessi di localizzazione sono garantiti.
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Ottiene la posizione corrente dell'utente.
     */
    suspend fun getCurrentLocation(): LatLng? = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission()) {
            Log.w(TAG, "Location permission not granted")
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(2000L)
                .setMaxUpdateDelayMillis(LOCATION_TIMEOUT)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        Log.d(TAG, "Location obtained: ${location.latitude}, ${location.longitude}")
                        continuation.resume(LatLng(location.latitude, location.longitude))
                    } else {
                        Log.w(TAG, "Location result is null")
                        continuation.resume(null)
                    }
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }

            // Prova prima con l'ultima posizione conosciuta
            fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
                if (lastLocation != null && isLocationRecent(lastLocation)) {
                    Log.d(TAG, "Using last known location: ${lastLocation.latitude}, ${lastLocation.longitude}")
                    continuation.resume(LatLng(lastLocation.latitude, lastLocation.longitude))
                } else {
                    // Se non c'è una posizione recente, richiedi una nuova localizzazione
                    Log.d(TAG, "Requesting fresh location")
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        null
                    )

                    // Timeout handler
                    continuation.invokeOnCancellation {
                        fusedLocationClient.removeLocationUpdates(locationCallback)
                    }
                }
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Failed to get last location", exception)
                continuation.resume(null)
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception when requesting location", e)
            continuation.resume(null)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error when requesting location", e)
            continuation.resume(null)
        }
    }

    /**
     * Controlla se una posizione è considerata recente (entro 5 minuti).
     */
    private fun isLocationRecent(location: Location): Boolean {
        val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)
        return location.time > fiveMinutesAgo
    }
}