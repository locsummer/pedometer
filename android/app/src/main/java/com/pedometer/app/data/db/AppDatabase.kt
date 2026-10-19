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

@Database(entities = [StepRecord::class, Workout::class], version = 2, exportSchema = false)
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
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // Clean up any old mock/sample data if present
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                cleanupMockDataIfNeeded(database.stepDao())
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun cleanupMockDataIfNeeded(dao: StepDao) {
            val records = dao.getAllRecords()
            val mockSteps = setOf(11240, 8950, 10420, 13100, 9800, 14500, 10800, 7800, 11500, 13900, 10200, 9500, 12600, 11100)
            if (records.any { it.stepCount in mockSteps }) {
                dao.clearAllRecords()
            }
        }
    }
}
