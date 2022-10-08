package com.rshea.geofencing.util

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.rshea.geofencing.ui.view.MapsActivity
import com.rshea.geofencing.util.Constants.BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
import com.rshea.geofencing.util.Constants.LOCATION_PERMISSION_REQUEST_CODE
import com.vmadalin.easypermissions.EasyPermissions

object Permissions {

    fun hasLocationPermission(context: Context): Boolean {
        return EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun requestLocationPermission(activity: MapsActivity) {
        EasyPermissions.requestPermissions(
            activity,
            "This application cannot work without Location Permission.",
            LOCATION_PERMISSION_REQUEST_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun hasBackgroundLocationRequest(context: Context): Boolean {
        return EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestBackgroundLocationPermission(activity: MapsActivity) {
        EasyPermissions.requestPermissions(
            activity,
            "Background location permission is essential to this application. Without it we will not be able to provide you with our service.",
            BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    }

}