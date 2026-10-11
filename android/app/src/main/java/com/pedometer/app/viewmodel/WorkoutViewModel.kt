package com.pedometer.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pedometer.app.data.db.AppDatabase
import com.pedometer.app.data.model.Workout
import com.pedometer.app.data.model.WorkoutType
import com.pedometer.app.service.LocationTrackingManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val locationManager = LocationTrackingManager(application)

    private val _isWorkoutActive = MutableStateFlow(false)
    val isWorkoutActive: StateFlow<Boolean> = _isWorkoutActive.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _selectedType = MutableStateFlow(WorkoutType.WALK)
    val selectedType: StateFlow<WorkoutType> = _selectedType.asStateFlow()

    private val _currentWorkout = MutableStateFlow(Workout())
    val currentWorkout: StateFlow<Workout> = _currentWorkout.asStateFlow()

    val pastWorkouts: StateFlow<List<Workout>> = database.workoutDao().observeAllWorkouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routePoints = locationManager.routePoints
    val currentSpeed = locationManager.currentSpeed
    val totalDistance = locationManager.totalDistanceMeters
    val elevationGain = locationManager.elevationGainMeters

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            combine(routePoints, totalDistance, elevationGain) { pts, dist, elev ->
                if (_isWorkoutActive.value && !_isPaused.value) {
                    val stepMultiplier = if (_selectedType.value == WorkoutType.RUN) 0.9 else 1.3
                    val steps = (dist * stepMultiplier).toInt()
                    val calMultiplier = if (_selectedType.value == WorkoutType.RUN) 0.065 else 0.045
                    val calories = steps * calMultiplier

                    _currentWorkout.value = _currentWorkout.value.copy(
                        routePoints = pts,
                        distanceMeters = dist,
                        stepCount = steps,
                        elevationGainMeters = elev,
                        caloriesBurned = calories
                    )
                }
            }.collect()
        }
    }

    fun setWorkoutType(type: WorkoutType) {
        if (!_isWorkoutActive.value) {
            _selectedType.value = type
        }
    }

    fun startWorkout() {
        _currentWorkout.value = Workout(type = _selectedType.value, startTimestamp = System.currentTimeMillis())
        _isWorkoutActive.value = true
        _isPaused.value = false
        locationManager.startTracking()

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isWorkoutActive.value) {
                delay(1000)
                if (!_isPaused.value) {
                    _currentWorkout.value = _currentWorkout.value.copy(
                        durationSeconds = _currentWorkout.value.durationSeconds + 1
                    )
                }
            }
        }
    }

    fun pauseWorkout() {
        _isPaused.value = true
    }

    fun resumeWorkout() {
        _isPaused.value = false
    }

    fun stopWorkout() {
        timerJob?.cancel()
        locationManager.stopTracking()
        _isWorkoutActive.value = false
        _isPaused.value = false

        val finished = _currentWorkout.value.copy(endTimestamp = System.currentTimeMillis())
        if (finished.durationSeconds > 10 || finished.distanceMeters > 50) {
            viewModelScope.launch(Dispatchers.IO) {
                database.workoutDao().insertWorkout(finished)
            }
        }
    }
}
