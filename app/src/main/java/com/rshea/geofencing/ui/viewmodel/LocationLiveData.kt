package com.rshea.geofencing.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.android.gms.location.*
import com.rshea.geofencing.data.datasources.dto.LocationEntity
import com.rshea.geofencing.util.Constants.LOCATION_GEOFENCE_TRANSITION_ID
import com.rshea.geofencing.util.Constants.LOCATION_REQUEST_INTERVAL

class LocationLiveData(
    application: Application
): LiveData<LocationEntity>() {

    private var transitionStatePref: SharedPreferences? = application.getSharedPreferences(LOCATION_GEOFENCE_TRANSITION_ID, Context.MODE_PRIVATE)
    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)

    override fun onInactive() {
        super.onInactive()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    override fun onActive() {
        super.onActive()
        transitionStatePref?.edit()?.putInt(LOCATION_GEOFENCE_TRANSITION_ID, Geofence.GEOFENCE_TRANSITION_EXIT)?.apply()
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            location: Location -> location.also {
                setLocationLiveData(it)
            }
        }
        startLocationUpdate()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdate() {
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        ).addOnFailureListener {
            it.message?.let { msg -> Log.e(TAG, msg) }
        }
    }

    private val locationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            for (location in locationResult.locations) {
                setLocationLiveData(location)
            }
        }
    }

    private fun setLocationLiveData(location: Location) {
        value = transitionStatePref?.let {
            LocationEntity(location.latitude.toString(), location.longitude.toString(),
                it.getInt(LOCATION_GEOFENCE_TRANSITION_ID, Geofence.GEOFENCE_TRANSITION_EXIT))
        }
    }

    companion object {
        private const val TAG = "LocationLiveData"
        var locationRequest: LocationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_REQUEST_INTERVAL
        ).build()
    }
}