package com.rshea.geofencing.data.datasources.dto

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import com.google.android.gms.location.*
import com.rshea.geofencing.util.Constants
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import javax.inject.Singleton


@Singleton
class LocationDataSource(
    application: Application) {

    private val locationSubject = PublishSubject.create<LocationEntity>()
    private var transitionStatePref: SharedPreferences? = application.getSharedPreferences(
        Constants.LOCATION_GEOFENCE_TRANSITION_ID, Context.MODE_PRIVATE)
    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)

    val locationObservable: Flowable<LocationEntity> = locationSubject.toFlowable(
        BackpressureStrategy.MISSING)
        .doOnSubscribe { startLocationUpdate() }
        .doOnCancel { stopLocationUpdate() }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdate() {
        transitionStatePref?.edit()?.putInt(Constants.LOCATION_GEOFENCE_TRANSITION_ID, Geofence.GEOFENCE_TRANSITION_EXIT)?.apply()
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                location: Location -> location.also {
                    setLocationLiveData(it)
                }
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        ).addOnFailureListener {
            //in case of exception, close the Flow
        }
    }

    private fun stopLocationUpdate() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
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
        transitionStatePref?.let {
            LocationEntity(location.latitude.toString(), location.longitude.toString(),
                it.getInt(Constants.LOCATION_GEOFENCE_TRANSITION_ID, Geofence.GEOFENCE_TRANSITION_EXIT))
        }?.let {
            locationSubject.onNext(
                it
            )
        }
    }

    companion object {
        var locationRequest: LocationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            Constants.LOCATION_REQUEST_INTERVAL
        ).build()
    }

}