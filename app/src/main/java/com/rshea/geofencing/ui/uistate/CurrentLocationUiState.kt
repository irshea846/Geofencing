package com.rshea.geofencing.ui.uistate

import com.google.android.gms.maps.model.LatLng

data class CurrentLocationUiState(
    val latLng: LatLng? = null,
    val isInGeofencingArea: Boolean = false
)