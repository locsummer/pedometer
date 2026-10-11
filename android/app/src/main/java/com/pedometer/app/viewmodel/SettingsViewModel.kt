package com.pedometer.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pedometer.app.data.model.AppSettings
import com.pedometer.app.data.preferences.UserPreferences
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = UserPreferences(application)
    val settings: StateFlow<AppSettings> = preferences.settingsFlow

    fun updateSettings(newSettings: AppSettings) {
        preferences.saveSettings(newSettings)
    }

    fun resetDefaults() {
        preferences.saveSettings(AppSettings())
    }
}
