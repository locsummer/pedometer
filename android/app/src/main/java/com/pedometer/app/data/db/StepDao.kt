package com.pedometer.app.data.db

import androidx.room.*
import com.pedometer.app.data.model.StepRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM step_records WHERE date = :date LIMIT 1")
    suspend fun getRecordForDate(date: String): StepRecord?

    @Query("SELECT * FROM step_records WHERE date = :date LIMIT 1")
    fun observeRecordForDate(date: String): Flow<StepRecord?>

    @Query("SELECT * FROM step_records ORDER BY date DESC LIMIT 30")
    fun observeRecentRecords(): Flow<List<StepRecord>>

    @Query("SELECT * FROM step_records ORDER BY date DESC")
    suspend fun getAllRecords(): List<StepRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: StepRecord)

    @Query("UPDATE step_records SET stepCount = :steps, distanceMeters = :dist, activeCalories = :cal, activeMinutes = :mins WHERE date = :date")
    suspend fun updateSteps(date: String, steps: Int, dist: Double, cal: Double, mins: Int)

    @Query("DELETE FROM step_records")
    suspend fun clearAllRecords()
}
