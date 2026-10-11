package com.pedometer.app.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.pedometer.app.MainActivity
import com.pedometer.app.R
import com.pedometer.app.data.db.AppDatabase
import com.pedometer.app.data.model.StepRecord
import com.pedometer.app.data.preferences.UserPreferences
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.LocalTime

class PedometerForegroundService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private lateinit var sensorManager: StepSensorManager
    private lateinit var database: AppDatabase
    private lateinit var preferences: UserPreferences

    companion object {
        const val CHANNEL_ID = "pedometer_step_channel"
        const val NOTIFICATION_ID = 1001

        fun startService(context: Context) {
            val intent = Intent(context, PedometerForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        preferences = UserPreferences(this)
        sensorManager = StepSensorManager(this)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification(0, preferences.loadSettings().stepGoal))

        observeSensorSteps()
    }

    private fun observeSensorSteps() {
        sensorManager.startListening { }

        serviceScope.launch {
            sensorManager.stepEvents.collect { deltaSteps ->
                val todayStr = LocalDate.now().toString()
                val currentHour = LocalTime.now().hour
                val settings = preferences.loadSettings()

                val record = database.stepDao().getRecordForDate(todayStr) ?: StepRecord(date = todayStr)
                val newStepCount = record.stepCount + deltaSteps
                val newDistance = newStepCount * (settings.strideLengthCm / 100.0)
                val newCalories = newStepCount * 0.042
                val newActiveMinutes = newStepCount / 100

                val updatedHourly = record.hourlySteps.toMutableList()
                if (currentHour in updatedHourly.indices) {
                    updatedHourly[currentHour] = updatedHourly[currentHour] + deltaSteps
                }

                val updatedRecord = record.copy(
                    stepCount = newStepCount,
                    distanceMeters = newDistance,
                    activeCalories = newCalories,
                    activeMinutes = newActiveMinutes,
                    hourlySteps = updatedHourly
                )

                database.stepDao().insertOrUpdate(updatedRecord)
                updateNotification(newStepCount, settings.stepGoal)
            }
        }
    }

    private fun buildNotification(steps: Int, goal: Int): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val progress = if (goal > 0) (steps * 100 / goal) else 0

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pedometer++ : $steps steps")
            .setContentText("Daily Goal: $goal steps ($progress% completed)")
            .setSmallIcon(R.drawable.ic_walk)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setProgress(goal, steps, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(steps: Int, goal: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(steps, goal))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pedometer Active Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live step count in status bar"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.stopListening()
        serviceScope.cancel()
    }
}
