package com.rshea.geofencing.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rshea.geofencing.data.datasources.dao.GeofenceEntity
import com.rshea.geofencing.data.datasources.dto.LocationEntity
import com.rshea.geofencing.data.datasources.dto.LocationLiveData
import com.rshea.geofencing.data.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    application: Application,
    private var locationRepository: LocationRepository
): AndroidViewModel(application) {

    fun getLocationLiveData() =
        locationRepository.getLocationLiveData()

    fun getLocationRequest() =
        locationRepository.getLocationRequest()

}