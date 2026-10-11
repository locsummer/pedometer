package com.pedometer.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.pedometer.app.data.model.AppSettings
import com.pedometer.app.data.model.AppThemeColor
import com.pedometer.app.data.model.ColorMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("pedometer_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<AppSettings> = _settingsFlow.asStateFlow()

    fun loadSettings(): AppSettings {
        return AppSettings(
            stepGoal = prefs.getInt("step_goal", 10000),
            floorGoal = prefs.getInt("floor_goal", 10),
            useMetricUnits = prefs.getBoolean("use_metric", true),
            themeColor = try {
                AppThemeColor.valueOf(prefs.getString("theme_color", AppThemeColor.GREEN.name)!!)
            } catch (e: Exception) {
                AppThemeColor.GREEN
            },
            colorMode = try {
                ColorMode.valueOf(prefs.getString("color_mode", ColorMode.DARK.name)!!)
            } catch (e: Exception) {
                ColorMode.DARK
            },
            enableNotifications = prefs.getBoolean("notifications_enabled", true),
            strideLengthCm = prefs.getFloat("stride_length", 75.0f).toDouble()
        )
    }

    fun saveSettings(settings: AppSettings) {
        prefs.edit().apply {
            putInt("step_goal", settings.stepGoal)
            putInt("floor_goal", settings.floorGoal)
            putBoolean("use_metric", settings.useMetricUnits)
            putString("theme_color", settings.themeColor.name)
            putString("color_mode", settings.colorMode.name)
            putBoolean("notifications_enabled", settings.enableNotifications)
            putFloat("stride_length", settings.strideLengthCm.toFloat())
            apply()
        }
        _settingsFlow.value = settings
    }
}
