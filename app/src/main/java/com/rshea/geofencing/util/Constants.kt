package com.rshea.geofencing.util

object Constants {

    const val LOCATION_ENABLE_ALERT = "Please enable Location Settings."

    // PERMISSION REQUEST CODE
    const val LOCATION_PERMISSION_REQUEST_CODE = 1
    const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 2

    // SHARED PREFERENCE
    const val PREFERENCE_NAME = "geofence_preference"
    const val PREFERENCE_FIRST_LAUNCH = "firstLaunch"

    // Location
    const val LOCATION_REQUEST_INTERVAL = 1000L

    // Geofence
    const val GEOFENCE_PENDING_INTENT_REQUEST_CODE = 200
    const val GEOFENCE_LOITERING_DELAY = 5000

    const val GEOFENCE_ADDED = "Geofence added successfully!!!"
    const val GEOFENCE_ID = 100L
    const val GEOFENCE_NAME = "my_geofence"
    const val GEOFENCE_LOCATION_NAME = "current_geofence"
    const val GEOFENCE_RADIUS = 100.0f
    const val GEOFENCE_CIRCLE_STROKE_WIDTH = 4.0f

    const val GEOFENCE_DATABASE_TABLE_NAME = "geofence_table"
    const val GEOFENCE_DATABASE_NAME = "geofence_db"

    // Notification
    const val NOTIFICATION_CHANNEL_ID = "geofence_transition_id"
    const val NOTIFICATION_CHANNEL_NAME = "geofence_notification"
    const val NOTIFICATION_ID = 3
}