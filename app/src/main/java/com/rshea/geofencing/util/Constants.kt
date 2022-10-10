package com.rshea.geofencing.util

object Constants {

    const val GEOFENCE_ADDED = "Geofence added successfully!!!"
    const val GEOFENCE_ID = "current_geofence"
    const val GEOFENCE_RADIUS = 100.0f

    const val LOCATION_PERMISSION_REQUEST_CODE = 1
    const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 2

    const val PREFERENCE_NAME = "geofence_preference"
    const val PREFERENCE_FIRST_LAUNCH = "firstLaunch"

    const val PENDING_INTENT_REQUEST_CODE = 200
    const val LOITERING_DELAY = 5000

    const val DATABASE_TABLE_NAME = "geofence_table"
    const val DATABASE_NAME = "geofence_db"

    const val NOTIFICATION_CHANNEL_ID = "geofence_transition_id"
    const val NOTIFICATION_CHANNEL_NAME = "geofence_notification"
    const val NOTIFICATION_ID = 3
}