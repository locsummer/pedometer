package com.pedometer.app.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import com.pedometer.app.data.model.LocationPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationTrackingManager(context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    private val _routePoints = MutableStateFlow<List<LocationPoint>>(emptyList())
    val routePoints: StateFlow<List<LocationPoint>> = _routePoints.asStateFlow()

    private val _currentSpeed = MutableStateFlow(0f)
    val currentSpeed: StateFlow<Float> = _currentSpeed.asStateFlow()

    private val _totalDistanceMeters = MutableStateFlow(0.0)
    val totalDistanceMeters: StateFlow<Double> = _totalDistanceMeters.asStateFlow()

    private val _elevationGainMeters = MutableStateFlow(0.0)
    val elevationGainMeters: StateFlow<Double> = _elevationGainMeters.asStateFlow()

    private var lastLocation: Location? = null
    var isTracking: Boolean = false
        private set

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            if (location.accuracy > 30) return // filter inaccurate points

            _currentSpeed.value = location.speed.coerceAtLeast(0f)

            lastLocation?.let { prev ->
                val dist = prev.distanceTo(location).toDouble()
                if (dist > 1.5) { // filter jitter
                    _totalDistanceMeters.value += dist
                    val deltaAlt = location.altitude - prev.altitude
                    if (deltaAlt > 0) {
                        _elevationGainMeters.value += deltaAlt
                    }
                }
            }
            lastLocation = location

            val newPoint = LocationPoint(
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = location.altitude,
                timestamp = location.time,
                speed = location.speed
            )
            _routePoints.value = _routePoints.value + newPoint
        }
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        _routePoints.value = emptyList()
        _totalDistanceMeters.value = 0.0
        _elevationGainMeters.value = 0.0
        lastLocation = null
        isTracking = true

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateDistanceMeters(2f)
            .build()

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun stopTracking() {
        isTracking = false
        _currentSpeed.value = 0f
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
