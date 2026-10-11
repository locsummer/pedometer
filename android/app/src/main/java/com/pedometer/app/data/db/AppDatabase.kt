package com.pedometer.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pedometer.app.data.model.StepRecord
import com.pedometer.app.data.model.Workout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Database(entities = [StepRecord::class, Workout::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stepDao(): StepDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pedometer_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate sample historical data
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                populateSampleData(database.stepDao())
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateSampleData(dao: StepDao) {
            val today = LocalDate.now()
            val sampleSteps = listOf(11240, 8950, 10420, 13100, 9800, 14500, 10800, 7800, 11500, 13900, 10200, 9500, 12600, 11100)
            
            sampleSteps.forEachIndexed { index, steps ->
                val date = today.minusDays(index.toLong()).toString()
                val dist = steps * 0.75
                val floors = (4..16).random()
                val cal = steps * 0.042
                val mins = steps / 105
                val hourly = (0..23).map { h ->
                    if (h in 7..21) (steps / 15 + (-50..50).random()).coerceAtLeast(0) else 0
                }
                dao.insertOrUpdate(
                    StepRecord(
                        date = date,
                        stepCount = steps,
                        distanceMeters = dist,
                        floorsAscended = floors,
                        floorsDescended = (floors - 2).coerceAtLeast(1),
                        activeCalories = cal,
                        activeMinutes = mins,
                        hourlySteps = hourly
                    )
                )
            }
        }
    }
}
