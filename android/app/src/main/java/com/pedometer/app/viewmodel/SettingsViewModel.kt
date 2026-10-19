package com.pedometer.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pedometer.app.data.db.AppDatabase
import com.pedometer.app.data.model.AppSettings
import com.pedometer.app.data.preferences.UserPreferences
import com.pedometer.app.service.StepSensorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val preferences = UserPreferences(application)
    private val sensorManager = StepSensorManager(application)

    val settings: StateFlow<AppSettings> = preferences.settingsFlow
    val activeSensorName: String = sensorManager.activeSensorName

    fun updateSettings(newSettings: AppSettings) {
        preferences.saveSettings(newSettings)
    }

    fun resetDefaults() {
        preferences.saveSettings(AppSettings())
    }

    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            database.stepDao().clearAllRecords()
            sensorManager.resetSensorBaseline()
        }
    }
}
