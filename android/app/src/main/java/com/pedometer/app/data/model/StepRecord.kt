package com.pedometer.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "step_records")
data class StepRecord(
    @PrimaryKey
    val date: String = LocalDate.now().toString(), // YYYY-MM-DD
    val stepCount: Int = 0,
    val distanceMeters: Double = 0.0,
    val floorsAscended: Int = 0,
    val floorsDescended: Int = 0,
    val activeCalories: Double = 0.0,
    val activeMinutes: Int = 0,
    val hourlySteps: List<Int> = List(24) { 0 }
) {
    val distanceKilometers: Double
        get() = distanceMeters / 1000.0

    val distanceMiles: Double
        get() = distanceMeters * 0.000621371

    fun formattedDistance(useMetric: Boolean): String {
        return if (useMetric) {
            String.format("%.2f km", distanceKilometers)
        } else {
            String.format("%.2f mi", distanceMiles)
        }
    }
}
