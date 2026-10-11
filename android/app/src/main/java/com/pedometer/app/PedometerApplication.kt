package com.pedometer.app

import android.app.Application
import com.pedometer.app.data.db.AppDatabase
import com.pedometer.app.service.PedometerForegroundService

class PedometerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Room database singleton
        AppDatabase.getDatabase(this)
        
        // Start background pedometer foreground service
        try {
            PedometerForegroundService.startService(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
