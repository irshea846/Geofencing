package com.rshea.geofencing.data.repository

import com.rshea.geofencing.data.datasources.dao.GeofenceDao
import com.rshea.geofencing.data.datasources.dao.GeofenceEntity
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ViewModelScoped
class GeofenceRepository @Inject constructor(private val geofenceDao: GeofenceDao) {

    val readGeofence: Flow<MutableList<GeofenceEntity>> = geofenceDao.readGeofence()

    suspend fun addGeofence(geofenceEntity: GeofenceEntity) {
        geofenceDao.addGeofence(geofenceEntity)
    }

    suspend fun removeGeofence(geofenceEntity: GeofenceEntity) {
        geofenceDao.removeGeofence(geofenceEntity)
    }
}