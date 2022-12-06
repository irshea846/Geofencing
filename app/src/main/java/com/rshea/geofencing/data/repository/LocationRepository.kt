package com.rshea.geofencing.data.repository

import com.rshea.geofencing.domain.model.LocationDomainModel
import io.reactivex.Flowable

interface LocationRepository {

    fun getLocation(): Flowable<LocationDomainModel>

}