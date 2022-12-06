package com.rshea.geofencing.ui.model

import com.rshea.geofencing.domain.model.LocationDomainModel

data class LocationModel(
    val lat: Double,
    val lng: Double,
    val transitionState: Int
)

fun LocationDomainModel.mapToPresentation(transitionState: Int): LocationModel {
    return LocationModel(lat, lng, transitionState)
}