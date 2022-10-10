package com.rshea.geofencing.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.rshea.geofencing.broadcastreceiver.GeofenceBroadcastReceiver
import com.rshea.geofencing.data.repository.GeofenceRepository
import com.rshea.geofencing.ui.uistate.CurrentLocationUiState
import com.rshea.geofencing.util.Constants.GEOFENCE_ADDED
import com.rshea.geofencing.util.Constants.GEOFENCE_ID
import com.rshea.geofencing.util.Constants.GEOFENCE_RADIUS
import com.rshea.geofencing.util.Constants.LOITERING_DELAY
import com.rshea.geofencing.util.Constants.PENDING_INTENT_REQUEST_CODE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    application: Application,
    private val geofenceRepository: GeofenceRepository
    //private val locationRepository: LocationRepository
): AndroidViewModel(application) {

    companion object {
        private const val TAG = "SharedViewModel"
    }

    private var app = application
    private var pendingIntent: PendingIntent? = null
    private var geofencingClient = LocationServices.getGeofencingClient(app)

    private val _uiState = MutableStateFlow(CurrentLocationUiState())
    val uiState: StateFlow<CurrentLocationUiState> = _uiState.asStateFlow()

    // fun readCurrentLocationUIState(): CurrentLocationUiState

    private fun geofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
        }.build()
    }

    private fun getGeofence(latLng: LatLng): Geofence {
        return Geofence.Builder().apply {
            setCircularRegion(latLng.latitude, latLng.longitude, GEOFENCE_RADIUS)
            setRequestId(GEOFENCE_ID)
            setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL
                    or Geofence.GEOFENCE_TRANSITION_EXIT).setLoiteringDelay(LOITERING_DELAY)
            setExpirationDuration(Geofence.NEVER_EXPIRE)
        }.build()
    }

    private fun geofencePendingIntent(): PendingIntent {
        if (pendingIntent != null) {
            return pendingIntent as PendingIntent
        }
        val intent = Intent(app, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        var flag = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flag = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        }
        pendingIntent = PendingIntent.getBroadcast(app, PENDING_INTENT_REQUEST_CODE, intent, flag)
        return pendingIntent as PendingIntent
    }

    @SuppressLint("MissingPermission")
    fun addGeofence(latLng: LatLng) {
        val geofence = getGeofence(
            latLng,
            )
        val geofenceRequest = geofencingRequest(geofence)
        val pendingIntent = geofencePendingIntent()
        geofencingClient.addGeofences(geofenceRequest, pendingIntent).run {
            addOnSuccessListener {
                Log.i(TAG, GEOFENCE_ADDED)
            }
            addOnFailureListener {
                val error = getErrorMessage(it)
                Log.i(TAG, error)
            }
        }
    }

    private fun getErrorMessage(e: Exception): String {
        if (e is ApiException) run {
            when (e.statusCode) {
                GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> return GeofenceStatusCodes.getStatusCodeString(
                    GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE)
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> return GeofenceStatusCodes.getStatusCodeString(
                    GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES)
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS ->
                    return GeofenceStatusCodes.getStatusCodeString(GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS)
                else -> { return GeofenceStatusCodes.getStatusCodeString(GeofenceStatusCodes.ERROR) }
            }
        } else {
            return GeofenceStatusCodes.getStatusCodeString(GeofenceStatusCodes.ERROR)
        }
    }

    fun checkDeviceLocationSettings(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val locationManager =
                app.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.isLocationEnabled
        } else {
            val mode: Int = Settings.Secure.getInt(
                app.contentResolver,
                Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            )
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }
}