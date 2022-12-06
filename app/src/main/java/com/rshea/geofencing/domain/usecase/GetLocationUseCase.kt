package com.rshea.geofencing.domain.usecase

import com.rshea.geofencing.data.repository.LocationRepository
import com.rshea.geofencing.domain.model.LocationDomainModel
import io.reactivex.Flowable
import javax.inject.Inject

class GetLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    fun build() : Flowable<LocationDomainModel> {
        return locationRepository.getLocation()
    }
}