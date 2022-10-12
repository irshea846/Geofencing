package com.rshea.geofencing.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.rshea.geofencing.data.datasources.dto.LocationLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    application: Application,
): AndroidViewModel(application) {

    private val locationLiveData = LocationLiveData(application)
    fun getLocationLiveData() = locationLiveData
    fun getLocationRequest() = locationLiveData

}