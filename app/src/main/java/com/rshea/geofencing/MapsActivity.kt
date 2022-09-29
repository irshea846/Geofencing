package com.rshea.geofencing

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.rshea.geofencing.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var mGeofencingClient: GeofencingClient
    private lateinit var binding: ActivityMapsBinding
    private var permissionDenied = false
    private lateinit var mGeofenceHelper: GeofenceHelper

    companion object {
        private const val TAG = "MapsActivity"
        private const val GEOFENCE_ADDED = "Geofence added successfully!!!"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val GEOFENCE_RADIUS = 100.0f
        private const val ZOOM_RADIUS = 16.0f
        private val RISE_CAFE = object {
            val lat = 37.7737
            val lng = -122.4662
        }
        private const val GEOFENCE_ID = "RISE_CAFE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mGeofencingClient = LocationServices.getGeofencingClient(this)
        mGeofenceHelper = GeofenceHelper(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        when (geofencingEvent.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Toast.makeText(this, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_LONG).show()
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                Toast.makeText(this, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_LONG).show()
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Toast.makeText(this, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_LONG).show()
            }
        }
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        //val sydney = LatLng(-34.0, 151.0)
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        val riseCafe = LatLng(RISE_CAFE.lat, RISE_CAFE.lng)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(riseCafe, ZOOM_RADIUS))
        //setOnCameraMoveListener(this)

        enableUserLocation()

        mMap.setOnMapLongClickListener(this)
    }

    @SuppressLint("MissingPermission")
    private fun enableUserLocation() {
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            mMap.isMyLocationEnabled = true
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (permissions.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation()
            } else {
                // Permission was denied. Display an error message
                // Display the missing permission error dialog when the fragments resume.
                permissionDenied = true
            }
        }
    }

    override fun onMapLongClick(latLng: LatLng?) {
        mMap.clear()
        mMap.addMarker(latLng?.let { MarkerOptions().position(it) })
        addCircle(latLng)
        if (latLng != null) {
            addGeofence(latLng)
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence(latLng: LatLng) {
        val geofence = mGeofenceHelper.getGeofence(GEOFENCE_ID, latLng, GEOFENCE_RADIUS,
            Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL
                    or Geofence.GEOFENCE_TRANSITION_EXIT)
        val geofenceRequest = mGeofenceHelper.geofencingRequest(geofence)
        val pendingIntent = mGeofenceHelper.geofencePendingIntent()
        mGeofencingClient.addGeofences(geofenceRequest, pendingIntent).run {
            addOnSuccessListener {
                Log.i(TAG, GEOFENCE_ADDED)
            }
            addOnFailureListener {
                val error = mGeofenceHelper.getErrorMessage(it)
                Log.i(TAG, error)
            }

        }
    }

    private fun addCircle(latLng: LatLng?) {
        val circleOptions: CircleOptions =
            CircleOptions().center(latLng).radius(GEOFENCE_RADIUS.toDouble())
                .strokeColor(Color.GREEN).fillColor(Color.argb(64, 0, 255, 0))
                .strokeWidth(4.0f)
        mMap.addCircle(circleOptions)

    }

    private fun setOnCameraMoveListener(ctx: Context) {

    }
}

