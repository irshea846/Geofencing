package com.rshea.geofencing.data.datasources.dto

data class LocationEntity(
    val lat: String,
    val lng: String,
    val transitionState: Int
    )