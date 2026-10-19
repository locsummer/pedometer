package com.pedometer.app.service

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.time.LocalDate
import kotlin.math.sqrt

class StepSensorManager(private val context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    private val accelSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val prefs: SharedPreferences = context.getSharedPreferences("pedometer_sensor_prefs", Context.MODE_PRIVATE)

    private val _stepEvents = MutableSharedFlow<Int>(
        replay = 0,
        extraBufferCapacity = 512,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val stepEvents: SharedFlow<Int> = _stepEvents.asSharedFlow()

    private var stepCallback: ((Int) -> Unit)? = null
    private var isListening = false

    // Fallback accelerometer peak detection variables
    private var lastMagnitude = 0.0
    private var accelThreshold = 11.5
    private var lastPeakTime = 0L

    val isHardwareStepSensorAvailable: Boolean
        get() = stepCounterSensor != null || stepDetectorSensor != null

    val activeSensorName: String
        get() = when {
            stepCounterSensor != null -> "Hardware Step Counter"
            stepDetectorSensor != null -> "Hardware Step Detector"
            accelSensor != null -> "Accelerometer (Calculated)"
            else -> "No Sensor Available"
        }

    fun startListening(onStepDetected: ((Int) -> Unit)? = null) {
        if (isListening) return
        stepCallback = onStepDetected
        isListening = true

        // Priority: Hardware Step Counter (most accurate & power-efficient)
        // Fallback 1: Hardware Step Detector
        // Fallback 2: Accelerometer Peak Detection
        when {
            stepCounterSensor != null -> {
                sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
            }
            stepDetectorSensor != null -> {
                sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_UI)
            }
            accelSensor != null -> {
                sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_GAME)
            }
        }
    }

    fun stopListening() {
        if (!isListening) return
        isListening = false
        stepCallback = null
        sensorManager.unregisterListener(this)
    }

    fun resetSensorBaseline() {
        prefs.edit().clear().apply()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val totalHardwareSteps = event.values[0].toInt()
                val todayStr = LocalDate.now().toString()

                val savedCount = prefs.getInt("last_hardware_step_count", -1)
                val savedDate = prefs.getString("last_sensor_date", null)

                val delta: Int = when {
                    savedCount == -1 -> {
                        // First event on fresh app run: establish baseline
                        0
                    }
                    savedDate != todayStr -> {
                        // Date changed (midnight rollover) while inactive
                        0
                    }
                    totalHardwareSteps < savedCount -> {
                        // Device rebooted, hardware counter reset to 0
                        totalHardwareSteps
                    }
                    else -> {
                        totalHardwareSteps - savedCount
                    }
                }

                prefs.edit()
                    .putInt("last_hardware_step_count", totalHardwareSteps)
                    .putString("last_sensor_date", todayStr)
                    .apply()

                if (delta > 0) {
                    _stepEvents.tryEmit(delta)
                    stepCallback?.invoke(delta)
                }
            }

            Sensor.TYPE_STEP_DETECTOR -> {
                if (event.values[0] == 1.0f) {
                    _stepEvents.tryEmit(1)
                    stepCallback?.invoke(1)
                }
            }

            Sensor.TYPE_ACCELEROMETER -> {
                // Accelerometer peak detection algorithm for devices without hardware step counter
                val x = event.values[0].toDouble()
                val y = event.values[1].toDouble()
                val z = event.values[2].toDouble()
                val magnitude = sqrt(x * x + y * y + z * z)
                val currentTime = System.currentTimeMillis()

                if (magnitude > accelThreshold && lastMagnitude <= accelThreshold) {
                    if (currentTime - lastPeakTime > 280) { // Max ~3.5 steps/second cadence
                        lastPeakTime = currentTime
                        _stepEvents.tryEmit(1)
                        stepCallback?.invoke(1)
                    }
                }
                lastMagnitude = magnitude
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
