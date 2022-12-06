package com.rshea.geofencing.data.repository

import com.rshea.geofencing.data.datasources.dto.LocationDataSource
import com.rshea.geofencing.domain.model.LocationDomainModel
import io.reactivex.Flowable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val locationDataSource: LocationDataSource
): LocationRepository {

    override fun getLocation(): Flowable<LocationDomainModel> {
        return locationDataSource
            .locationObservable
            .map { it.mapToDomain() }
    }

}