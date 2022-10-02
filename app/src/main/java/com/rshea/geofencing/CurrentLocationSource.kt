package com.rshea.geofencing

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener


class CurrentLocationSource(context: Context) : LocationSource, LocationListener {
    private val ctx: Context = context
    private var listener: OnLocationChangedListener? = null
    private val locationManager: LocationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    override fun activate(listener: OnLocationChangedListener) {
        this.listener = listener
        val isGpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (isGpsProvider)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10F, this)
        val isNetworkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (isNetworkProvider) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000 * 60 * 5,
                0F,
                this
            )
        }
    }

    override fun deactivate() {
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        if (listener != null) {
            listener!!.onLocationChanged(location)
        }
    }

    override fun onProviderDisabled(provider: String) {
        // TODO Auto-generated method stub
    }

    override fun onProviderEnabled(provider: String) {
        // TODO Auto-generated method stub
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {
        // TODO Auto-generated method stub
    }

}
