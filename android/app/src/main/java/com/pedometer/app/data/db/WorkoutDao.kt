package com.pedometer.app.data.db

import androidx.room.*
import com.pedometer.app.data.model.Workout
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY startTimestamp DESC")
    fun observeAllWorkouts(): Flow<List<Workout>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)
}
