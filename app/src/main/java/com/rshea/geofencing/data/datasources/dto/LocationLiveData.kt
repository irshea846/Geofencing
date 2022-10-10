package com.rshea.geofencing.data.datasources.dto

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.lifecycle.LiveData
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.rshea.geofencing.util.Constants.LOCATION_REQUEST_INTERVAL

class LocationLiveData(
    application: Application
): LiveData<LocationEntity>() {

    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)

    override fun onInactive() {
        super.onInactive()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    override fun onActive() {
        super.onActive()
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
            //in case of exception, close the Flow
        }
    }

    private val locationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)

            locationResult ?: return

            for (location in locationResult.locations) {
                setLocationLiveData(location)
            }
        }
    }

    private fun setLocationLiveData(location: Location) {
        value = LocationEntity(location.latitude.toString(), location.longitude.toString())
    }

    companion object {
        var locationRequest: LocationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = LOCATION_REQUEST_INTERVAL
            fastestInterval = LOCATION_REQUEST_INTERVAL / 4
        }
    }
}