package com.pedometer.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class WorkoutType(val title: String) {
    WALK("Walk"),
    RUN("Run"),
    HIKE("Hike")
}

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val timestamp: Long,
    val speed: Float
)

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val type: WorkoutType = WorkoutType.WALK,
    val startTimestamp: Long = System.currentTimeMillis(),
    val endTimestamp: Long? = null,
    val durationSeconds: Long = 0,
    val distanceMeters: Double = 0.0,
    val stepCount: Int = 0,
    val caloriesBurned: Double = 0.0,
    val elevationGainMeters: Double = 0.0,
    val routePoints: List<LocationPoint> = emptyList()
) {
    val formattedPace: String
        get() {
            if (distanceMeters < 50) return "--'--\""
            val km = distanceMeters / 1000.0
            val paceSecs = durationSeconds / km
            val mins = (paceSecs / 60).toInt()
            val secs = (paceSecs % 60).toInt()
            return String.format("%d'%02d\"/km", mins, secs)
        }

    val formattedDuration: String
        get() {
            val hours = durationSeconds / 3600
            val mins = (durationSeconds % 3600) / 60
            val secs = durationSeconds % 60
            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, mins, secs)
            } else {
                String.format("%02d:%02d", mins, secs)
            }
        }
}
