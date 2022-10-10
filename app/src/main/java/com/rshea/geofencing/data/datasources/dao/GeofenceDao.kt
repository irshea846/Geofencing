package com.rshea.geofencing.data.datasources.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GeofenceDao {

    @Query("SELECT * FROM geofence_table ORDER BY id ASC")
    fun readGeofence(): Flow<MutableList<GeofenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addGeofence(geofenceEntity: GeofenceEntity)

    @Delete
    suspend fun removeGeofence(geofenceEntity: GeofenceEntity)

}