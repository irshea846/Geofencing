package com.rshea.geofencing.domain.usecase

import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng
import com.rshea.geofencing.broadcastreceiver.GeofenceBroadcastReceiver

class GeofenceHelper(ctx: Context) : ContextWrapper(ctx) {
    private var pendingIntent: PendingIntent? = null

    companion object {
        private const val REQUEST_CODE = 200
        private const val LOITERING_DELAY = 5000
    }

    fun geofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
        }.build()
    }

    fun getGeofence(id: String, latLng: LatLng, radius: Float, transitionTypes: Int): Geofence {
        return Geofence.Builder().setCircularRegion(latLng.latitude, latLng.longitude,radius)
            .setRequestId(id).setTransitionTypes(transitionTypes).setLoiteringDelay(LOITERING_DELAY)
            .setExpirationDuration(Geofence.NEVER_EXPIRE).build()
    }

    fun geofencePendingIntent(): PendingIntent {
        if (pendingIntent != null) {
            return pendingIntent as PendingIntent
        }
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        var flag = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flag = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        }
        pendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, intent, flag)
        return pendingIntent as PendingIntent
    }

    fun getErrorMessage(e: Exception): String {
        if (e is ApiException) run {
            when (e.statusCode) {
                GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> return GeofenceStatusCodes.getStatusCodeString(GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE)
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> return GeofenceStatusCodes.getStatusCodeString(GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES)
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS ->
                        return GeofenceStatusCodes.getStatusCodeString(GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS)
                else -> { return GeofenceStatusCodes.getStatusCodeString(GeofenceStatusCodes.ERROR) }
            }
        } else {
            return GeofenceStatusCodes.getStatusCodeString(GeofenceStatusCodes.ERROR)
        }
    }
}