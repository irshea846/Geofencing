package com.rshea.geofencing.domain.model

data class LocationDomainModel(
    val lat: Double,
    val lng: Double,
    val transitionState: Int
)
