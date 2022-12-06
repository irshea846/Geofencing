package com.rshea.geofencing.data.datasources.dto

import com.rshea.geofencing.domain.model.LocationDomainModel

data class LocationEntity(
    val lat: String,
    val lng: String,
    val transitionState: Int
) {
    fun mapToDomain() : LocationDomainModel {
        return LocationDomainModel(
            lat.toDouble(),
            lng.toDouble(),
            transitionState
        )
    }
}
