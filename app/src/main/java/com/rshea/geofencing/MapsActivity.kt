package com.rshea.geofencing

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.rshea.geofencing.databinding.ActivityMapsBinding


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var mMap: GoogleMap
    private lateinit var mGeofencingClient: GeofencingClient
    private lateinit var mGeoFencePref: SharedPreferences
    private lateinit var mGeofenceHelper: GeofenceHelper
    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var mBlueMarkerDescriptor: BitmapDescriptor
    private lateinit var mGreenMarkerDescriptor: BitmapDescriptor
    private var mGeofenceTransitionState: Int = Geofence.GEOFENCE_TRANSITION_EXIT
    private lateinit var binding: ActivityMapsBinding
    private var permissionDenied = false
    private lateinit var locationRequest: LocationRequest

    private val strokeColor = Color.argb(255, 130, 182, 228)
    private val fillColor = Color.argb(100, 130, 182, 228)

    private var positionMarker: Marker? = null

    companion object {
        private const val TAG = "MapsActivity"
        private const val GEOFENCE_ADDED = "Geofence added successfully!!!"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 2
        private const val GEOFENCE_RADIUS = 100.0f
        private const val ZOOM_RADIUS = 16.0f
        private val RISE_CAFE = object {
            val lat = 37.7737
            val lng = -122.4662
        }
        private const val GEOFENCE_ID = "RISE_CAFE"
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this).build()

        mBlueMarkerDescriptor =
            BitmapDescriptorFactory.fromBitmap(getDrawable(R.drawable.ic_blue)!!.toBitmap(40, 60))
        mGreenMarkerDescriptor =
            BitmapDescriptorFactory.fromBitmap(getDrawable(R.drawable.ic_green)!!.toBitmap(40, 60))

        mGeofencingClient = LocationServices.getGeofencingClient(this)
        mGeofenceHelper = GeofenceHelper(this)

        mGeoFencePref = getSharedPreferences("TriggeredGeofenceTransitionStatus", MODE_PRIVATE)
        mGeoFencePref.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient.connect()
    }

    override fun onStop() {
        super.onStop()
        mGoogleApiClient.disconnect()
    }

    @SuppressLint("MissingPermission")
    override fun onConnected(bundle: Bundle?) {
        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 1000
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(
            locationRequest,
            object: LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    for (location in locationResult.locations) {
                        val latitude: Double = location.latitude
                        val longitude: Double = location.longitude
                        positionMarker?.remove()
                        val positionMarkerOptions = MarkerOptions()
                            .position(LatLng(latitude, longitude))
                            .anchor(0.5f, 0.5f)
                        if (mGeofenceTransitionState == Geofence.GEOFENCE_TRANSITION_EXIT) {
                            positionMarkerOptions.icon(mBlueMarkerDescriptor)
                        } else {
                            positionMarkerOptions.icon(mGreenMarkerDescriptor)
                        }
                        positionMarker = mMap.addMarker(positionMarkerOptions)
                    }
                }
            },
            Looper.getMainLooper()
        )
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onLocationChanged(location: Location) {
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key.equals("TriggeredGeofenceTransitionStatus")) {
            mGeofenceTransitionState = mGeoFencePref.getInt("TriggeredGeofenceTransitionStatus", Geofence.GEOFENCE_TRANSITION_EXIT)
            when (mGeofenceTransitionState) {
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
        val riseCafe = LatLng(RISE_CAFE.lat, RISE_CAFE.lng)
        mMap = googleMap
        mMap.addMarker(MarkerOptions().position(riseCafe).icon(mBlueMarkerDescriptor))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(riseCafe, ZOOM_RADIUS))
        enableUserLocation()
        mMap.setOnMapLongClickListener(this)
    }

    @SuppressLint("MissingPermission")
    private fun enableUserLocation() {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
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
        if (requestCode == BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE) {
            if (permissions.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "ACCESS_BACKGROUND_LOCATION Granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "ACCESS_BACKGROUND_LOCATION Denied", Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onMapLongClick(latLng: LatLng?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                handleMapLongClick(latLng)
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE)
                } else {
                    ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE)
                }
            }
        } else {
            handleMapLongClick(latLng)
        }
    }

    private fun handleMapLongClick(latLng: LatLng?) {
        mMap.clear()
        //mMap.addMarker(MarkerOptions().position(LatLng(RISE_CAFE.lat, RISE_CAFE.lng)).icon(markerDescriptor))
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
                .strokeColor(strokeColor).fillColor(fillColor)
                .strokeWidth(4.0f)
        mMap.addCircle(circleOptions)

    }
}

