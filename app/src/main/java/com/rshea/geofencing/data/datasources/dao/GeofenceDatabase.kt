package com.rshea.geofencing.data.datasources.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [GeofenceEntity::class],
    version = 1,
    exportSchema = false
)
abstract class GeofenceDatabase: RoomDatabase() {

    abstract fun geofenceDao(): GeofenceDao

}