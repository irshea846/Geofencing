package com.rshea.geofencing

import com.google.android.gms.location.Geofence
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.rshea.geofencing.data.datasources.dto.LocationEntity
import com.rshea.geofencing.ui.view.MapsActivity
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val latLng = LatLng(36.39487273, -122.9875487487)
        val locationEntity = LocationEntity(latLng.latitude.toString(), latLng.longitude.toString(), Geofence.GEOFENCE_TRANSITION_ENTER)
        val markerOptions: MarkerOptions = MapsActivity.Companion.parseFromLocationEntity(locationEntity)
        assert(latLng.latitude == markerOptions.position.latitude)
        assert(latLng.longitude == markerOptions.position.longitude)
    }
}