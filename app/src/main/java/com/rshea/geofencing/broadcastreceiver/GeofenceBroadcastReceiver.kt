package com.rshea.geofencing.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private var geoFencePref: SharedPreferences? = null

    companion object{
        private val TAG = GeofenceBroadcastReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context, intent: Intent) {
        geoFencePref = context.getSharedPreferences("TriggeredGeofenceTransitionStatus", Context.MODE_PRIVATE)
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, "error: $errorMessage")
            return
        }

        geoFencePref?.edit()?.putInt("TriggeredGeofenceTransitionStatus", geofencingEvent.geofenceTransition)?.apply()
    }
}