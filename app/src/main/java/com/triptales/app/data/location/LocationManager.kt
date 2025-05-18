package com.triptales.app.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit

/**
 * Manager per la gestione della geolocalizzazione dell'utente.
 * Utilizza Google Play Services per ottenere la posizione corrente.
 */
class LocationManager(private val context: Context) {

    companion object {
        private const val TAG = "LocationManager"
        private const val LOCATION_TIMEOUT = 15000L // 15 secondi

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
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocationPermission || coarseLocationPermission
    }

    /**
     * Ottiene la posizione corrente dell'utente con timeout migliorato.
     */
    suspend fun getCurrentLocation(): LatLng? {
        if (!hasLocationPermission()) {
            Log.w(TAG, "Location permission not granted")
            return null
        }

        return withTimeoutOrNull(LOCATION_TIMEOUT) {
            try {
                // Prima prova con l'ultima posizione conosciuta
                val lastLocation = getLastKnownLocation()
                if (lastLocation != null && isLocationRecent(lastLocation)) {
                    Log.d(TAG, "Using recent last location: ${lastLocation.latitude}, ${lastLocation.longitude}")
                    return@withTimeoutOrNull LatLng(lastLocation.latitude, lastLocation.longitude)
                }

                // Se non c'è una posizione recente, richiedi una nuova
                Log.d(TAG, "Requesting fresh location")
                getNewLocation()
            } catch (e: SecurityException) {
                Log.e(TAG, "Security exception when requesting location", e)
                null
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error when requesting location", e)
                null
            }
        }
    }

    /**
     * Ottiene l'ultima posizione conosciuta.
     */
    private suspend fun getLastKnownLocation(): Location? {
        return try {
            val deferred = CompletableDeferred<Location?>()

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    deferred.complete(location)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to get last location", exception)
                    deferred.complete(null)
                }

            deferred.await()
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception getting last location", e)
            null
        }
    }

    /**
     * Richiede una nuova posizione.
     */
    private suspend fun getNewLocation(): LatLng? {
        return try {
            val deferred = CompletableDeferred<LatLng?>()

            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(2000L)
                .setMaxUpdateDelayMillis(LOCATION_TIMEOUT)
                .setMinUpdateDistanceMeters(0f)
                .setGranularity(Granularity.GRANULARITY_FINE)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    val location = locationResult.lastLocation
                    if (location != null) {
                        Log.d(TAG, "Fresh location obtained: ${location.latitude}, ${location.longitude}")
                        Log.d(TAG, "Location accuracy: ${location.accuracy}m")
                        deferred.complete(LatLng(location.latitude, location.longitude))
                    } else {
                        Log.w(TAG, "Location result is null")
                        deferred.complete(null)
                    }
                    fusedLocationClient.removeLocationUpdates(this)
                }

                override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                    super.onLocationAvailability(locationAvailability)
                    if (!locationAvailability.isLocationAvailable) {
                        Log.w(TAG, "Location not available")
                        if (!deferred.isCompleted) {
                            deferred.complete(null)
                        }
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                context.mainLooper
            )

            deferred.await()
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception requesting new location", e)
            null
        }
    }

    /**
     * Controlla se una posizione è considerata recente (entro 2 minuti).
     */
    private fun isLocationRecent(location: Location): Boolean {
        val twoMinutesAgo = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2)
        return location.time > twoMinutesAgo
    }

    /**
     * Ottiene solo l'ultima posizione conosciuta senza richiederne una nuova.
     */
    suspend fun getLastKnownLocationOnly(): LatLng? {
        if (!hasLocationPermission()) {
            Log.w(TAG, "Location permission not granted")
            return null
        }

        val lastLocation = getLastKnownLocation()
        return if (lastLocation != null) {
            Log.d(TAG, "Last known location: ${lastLocation.latitude}, ${lastLocation.longitude}")
            LatLng(lastLocation.latitude, lastLocation.longitude)
        } else {
            Log.d(TAG, "No last known location available")
            null
        }
    }
}