package com.rshea.geofencing.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rshea.geofencing.domain.usecase.GetLocationUseCase
import com.rshea.geofencing.ui.model.LocationModel
import com.rshea.geofencing.ui.model.mapToPresentation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    application: Application,
    private val getLocationUseCase: GetLocationUseCase
): AndroidViewModel(application) {

    private val _locationModel = MutableLiveData<LocationModel>()
    val locationModel: LiveData<LocationModel> = _locationModel

    fun getLocationLiveData() {
        getLocationUseCase
            .build()
    }
}

//    private val locationLiveData = LocationLiveData(application)
//    fun getLocationLiveData() = locationLiveData
//    fun getLocationRequest() = locationLiveData
