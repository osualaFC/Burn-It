package com.example.burn_it.utils

import android.graphics.Color
import com.example.burn_it.R

object Constants {

    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"
    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1
    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 2000L
    const val POLYLINE_COLOR = R.color.colorPrimary
    const val POLYLINE_WIDTH = 8f
    const val MAP_ZOOM = 15f
    const val TIMER_UPDATE_INTERVAL = 50L
    const val RUNNING_DATABASE_NAME = "running_db"
    const val SHARED_PREFERENCES_NAME = "sharedPref"
    const val KEY_FIRST_TIME_TOGGLE = "KEY_FIRST_TIME_TOGGLE"
    const val KEY_NAME = "KEY_NAME"
    const val KEY_WEIGHT = "KEY_WEIGHT"
    const val API_KEY = "3b8def3e2c5f6e5a0596249d31f820ce"
    const val BASE_URL = "http://api.openweathermap.org/data/2.5/"
}