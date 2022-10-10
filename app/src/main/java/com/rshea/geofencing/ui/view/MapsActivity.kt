package com.rshea.geofencing.ui.view

import android.annotation.SuppressLint
import android.app.Application
import android.content.SharedPreferences
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.rshea.geofencing.R
import com.rshea.geofencing.databinding.ActivityMapsBinding
import com.rshea.geofencing.ui.viewmodel.SharedViewModel
import com.rshea.geofencing.util.Constants.BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
import com.rshea.geofencing.util.Constants.GEOFENCE_RADIUS
import com.rshea.geofencing.util.Constants.LOCATION_PERMISSION_REQUEST_CODE
import com.rshea.geofencing.util.Permissions
import com.rshea.geofencing.util.Permissions.hasBackgroundLocationRequest
import com.rshea.geofencing.util.Permissions.requestBackgroundLocationPermission
import com.rshea.geofencing.util.Permissions.requestLocationPermission
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, LocationListener, SharedPreferences.OnSharedPreferenceChangeListener, EasyPermissions.PermissionCallbacks {

    private lateinit var mMap: GoogleMap
    private lateinit var mGeoFencePref: SharedPreferences
    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var mBlueMarkerDescriptor: BitmapDescriptor
    private lateinit var mGreenMarkerDescriptor: BitmapDescriptor
    private var mGeofenceTransitionState: Int = Geofence.GEOFENCE_TRANSITION_EXIT
    private lateinit var mActivityMapsBinding: ActivityMapsBinding
    private lateinit var locationRequest: LocationRequest

    private var positionMarker: Marker? = null

    companion object {
        private const val TAG = "MapsActivity"
        private const val ZOOM_RADIUS = 16.0f
        private val RISE_CAFE = object {
            val lat = 37.7737
            val lng = -122.4662
        }
    }

    // Need to add implementation "androidx.fragment:fragment-ktx:1.5.2"
    private val sharedViewModel: SharedViewModel by viewModels()

    @Inject
    lateinit var randomString: String
    @Inject
    lateinit var app: Application

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "randomString: $randomString")
        Log.d(TAG, "app: $app")

        mActivityMapsBinding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(mActivityMapsBinding.root)

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
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(
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
                            .icon( if (mGeofenceTransitionState == Geofence.GEOFENCE_TRANSITION_EXIT) {
                                mBlueMarkerDescriptor
                            } else {
                                mGreenMarkerDescriptor
                            })
                        positionMarker = mMap.addMarker(positionMarkerOptions)
                    }
                }
            },
            Looper.getMainLooper()
        ).addOnFailureListener {
            //in case of exception, close the Flow
        }
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
        observeDatabase()
    }

    @SuppressLint("MissingPermission")
    private fun enableUserLocation() {
        if (Permissions.hasLocationPermission(this)) {
            //TODO: check first launch
        } else {
            requestLocationPermission(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(this).build().show()
        } else {
            when (requestCode) {
                LOCATION_PERMISSION_REQUEST_CODE -> requestLocationPermission(this)
                BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    requestBackgroundLocationPermission(this)
                }
                else -> TODO()
            }
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Toast.makeText(
            this,
            when (requestCode) {
                LOCATION_PERMISSION_REQUEST_CODE -> "ACCESS_LOCATION Granted"
                BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "ACCESS_BACKGROUND_LOCATION Granted" else TODO()
                else -> "No Access Granted"
            },
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onMapLongClick(latLng: LatLng) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (hasBackgroundLocationRequest(this)) {
                setupGeofence(latLng)
           } else {
                requestBackgroundLocationPermission(this)
            }
        } else {
            setupGeofence(latLng)
        }
    }

    private fun setupGeofence(location: LatLng) {
        lifecycleScope.launch {
            if (sharedViewModel.checkDeviceLocationSettings()) {
                addCircle(location)
                sharedViewModel.addGeofence(location)
                // TODO: zoomToGeofence(circle.center, circle.radius.toFloat())

                //delay(2000)
                //sharedViewModel.addGeofenceToDatabase(location)
                //delay(2000)
                //sharedViewModel.resetSharedValues()

            } else {
                Toast.makeText(
                    app,
                    "Please enable Location Settings.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun observeDatabase() {
//        sharedViewModel.readGeofences.observe(viewLifecycleOwner) { geofenceEntity ->
//            mMap.clear()
//            geofenceEntity.forEach { geofence ->
//                addCircle(LatLng(geofence.latitude, geofence.longitude))
//            }
//        }
    }


    private fun addCircle(latLng: LatLng) {
        val circleOptions: CircleOptions =
            CircleOptions().center(latLng).radius(GEOFENCE_RADIUS.toDouble())
                .strokeColor(resources.getColor(R.color.geofence_stroke_color, null))
                .fillColor(resources.getColor(R.color.geofence_fill_color, null))
                .strokeWidth(4.0f)
        mMap.addCircle(circleOptions)

    }
}

