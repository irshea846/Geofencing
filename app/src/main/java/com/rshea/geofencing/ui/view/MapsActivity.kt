package com.rshea.geofencing.ui.view

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
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
import com.rshea.geofencing.ui.viewmodel.GeofenceViewModel
import com.rshea.geofencing.ui.viewmodel.LocationViewModel
import com.rshea.geofencing.util.Constants.BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
import com.rshea.geofencing.util.Constants.CAMERA_ZOOM_RADIUS
import com.rshea.geofencing.util.Constants.GEOFENCE_CIRCLE_STROKE_WIDTH
import com.rshea.geofencing.util.Constants.GEOFENCE_RADIUS
import com.rshea.geofencing.util.Constants.LOCATION_ENABLE_ALERT
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
    GoogleApiClient.OnConnectionFailedListener, LocationListener, EasyPermissions.PermissionCallbacks {

    private lateinit var mMap: GoogleMap
    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var mBlueMarkerDescriptor: BitmapDescriptor
    private lateinit var mGreenMarkerDescriptor: BitmapDescriptor
    private lateinit var mActivityMapsBinding: ActivityMapsBinding

    companion object {
        private const val TAG = "MapsActivity"
        private val RISE_CAFE = object {
            val lat = 37.7737
            val lng = -122.4662
        }
    }

    // Need to add implementation "androidx.fragment:fragment-ktx:1.5.2"
    private val geofenceViewModel: GeofenceViewModel by viewModels()
    private val locationViewModel: LocationViewModel by viewModels()

    private var positionMarker: Marker? = null

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

    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient.connect()
    }

    override fun onStop() {
        super.onStop()
        mGoogleApiClient.disconnect()
    }

    override fun onConnected(bundle: Bundle?) {
        locationViewModel.getLocationRequest()
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onLocationChanged(location: Location) {
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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(riseCafe, CAMERA_ZOOM_RADIUS))
        enableUserLocation()
        mMap.setOnMapLongClickListener(this)
        observeDatabase()
    }

    @SuppressLint("MissingPermission")
    private fun enableUserLocation() {
        if (Permissions.hasLocationPermission(this)) {
            requestLocationUpdates()
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
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> requestLocationUpdates()
            BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE -> {
                Toast.makeText(
                    this,
                    "ACCESS_BACKGROUND_LOCATION Granted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun requestLocationUpdates() {
        locationViewModel.getLocationLiveData().observe(this) {
            val latitude: Double = it.lat.toDouble()
            val longitude: Double = it.lng.toDouble()
            val transitionState: Int = it.transitionState
            positionMarker?.remove()
            val positionMarkerOptions = MarkerOptions()
                .position(LatLng(latitude, longitude))
                .anchor(0.5f, 0.5f)
                .icon(
                    if (transitionState == Geofence.GEOFENCE_TRANSITION_EXIT)
                        mBlueMarkerDescriptor else
                        mGreenMarkerDescriptor
                )
            positionMarker = mMap.addMarker(positionMarkerOptions)
        }
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

    private fun setupGeofence(latLng: LatLng) {
        lifecycleScope.launch {
            if (geofenceViewModel.checkDeviceLocationSettings()) {
                geofenceViewModel.stopGeofence()
                mMap.clear()
                geofenceViewModel.startGeofence(latLng)
                // TODO: zoomToGeofence(circle.center, circle.radius.toFloat())
            } else {
                Toast.makeText(
                    app,
                    LOCATION_ENABLE_ALERT,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun observeDatabase() {
        geofenceViewModel.readGeofence.observe(this) { geofenceEntity ->
            mMap.clear()
            geofenceEntity.forEach { geofence ->
                addCircle(LatLng(geofence.latitude, geofence.longitude))
            }
        }
    }

    private fun addCircle(latLng: LatLng) {
        val circleOptions: CircleOptions =
            CircleOptions().center(latLng).radius(GEOFENCE_RADIUS.toDouble())
                .strokeColor(resources.getColor(R.color.geofence_stroke_color, null))
                .fillColor(resources.getColor(R.color.geofence_fill_color, null))
                .strokeWidth(GEOFENCE_CIRCLE_STROKE_WIDTH)
        mMap.addCircle(circleOptions)

    }
}
