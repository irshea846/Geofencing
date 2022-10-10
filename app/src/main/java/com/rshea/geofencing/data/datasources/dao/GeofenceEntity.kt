package com.rshea.geofencing.data.datasources.dao

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rshea.geofencing.util.Constants.GEOFENCE_DATABASE_TABLE_NAME
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Entity(tableName = GEOFENCE_DATABASE_TABLE_NAME)
@Parcelize //Need to plugins id 'kotlin-parcelize' in app/build.gradle
class GeofenceEntity(
    val geoId: Long,
    val name: String?,
    val location: String?,
    val latitude: Double,
    val longitude: Double,
    val radius: Float
): Parcelable {
    @IgnoredOnParcel
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}