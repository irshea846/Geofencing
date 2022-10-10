package com.rshea.geofencing.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.rshea.geofencing.util.Constants.LOCATION_GEOFENCE_TRANSITION_ID

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private var locGeoFencePref: SharedPreferences? = null

    companion object{
        private val TAG = GeofenceBroadcastReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context, intent: Intent) {
        locGeoFencePref = context.getSharedPreferences(LOCATION_GEOFENCE_TRANSITION_ID, Context.MODE_PRIVATE)
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, "error: $errorMessage")
            return
        }

        locGeoFencePref?.edit()?.putInt(LOCATION_GEOFENCE_TRANSITION_ID, geofencingEvent.geofenceTransition)?.apply()
        makeToastText(context, geofencingEvent.geofenceTransition)
    }

    private fun makeToastText(context: Context, geofenceTransition: Int) {
        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_LONG).show()
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                Toast.makeText(context, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_LONG).show()
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_LONG).show()
            }
        }
    }

}