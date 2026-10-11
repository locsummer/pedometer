package com.pedometer.app.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.math.sqrt

class StepSensorManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    private val accelSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _stepEvents = MutableSharedFlow<Int>(replay = 0)
    val stepEvents: SharedFlow<Int> = _stepEvents.asSharedFlow()

    private var initialCounterOffset = -1
    private var lastReportedCounter = 0

    // Fallback accelerometer peak detection variables
    private var lastMagnitude = 0.0
    private var accelThreshold = 11.5
    private var lastPeakTime = 0L

    val isHardwareStepSensorAvailable: Boolean
        get() = stepCounterSensor != null || stepDetectorSensor != null

    fun startListening(onStepDetected: (Int) -> Unit) {
        if (stepDetectorSensor != null) {
            sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else if (accelSensor != null) {
            sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_DETECTOR -> {
                if (event.values[0] == 1.0f) {
                    _stepEvents.tryEmit(1)
                }
            }
            Sensor.TYPE_STEP_COUNTER -> {
                val totalHardwareSteps = event.values[0].toInt()
                if (initialCounterOffset == -1) {
                    initialCounterOffset = totalHardwareSteps
                    lastReportedCounter = totalHardwareSteps
                }
                val delta = totalHardwareSteps - lastReportedCounter
                if (delta > 0) {
                    lastReportedCounter = totalHardwareSteps
                    _stepEvents.tryEmit(delta)
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
                    }
                }
                lastMagnitude = magnitude
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
