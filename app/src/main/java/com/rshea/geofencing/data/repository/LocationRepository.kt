package com.rshea.geofencing.data.repository

import android.app.Application
import com.rshea.geofencing.data.datasources.dto.LocationLiveData
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class LocationRepository @Inject constructor(
    application: Application
) {

    private val locationLiveData = LocationLiveData(application)
    fun getLocationLiveData() = locationLiveData
    fun getLocationRequest() = locationLiveData

}