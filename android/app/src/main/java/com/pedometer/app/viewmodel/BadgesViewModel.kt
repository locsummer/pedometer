package com.pedometer.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pedometer.app.data.db.AppDatabase
import com.pedometer.app.data.model.Badge
import com.pedometer.app.data.model.BadgeCategory
import com.pedometer.app.data.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BadgesViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val preferences = UserPreferences(application)

    private val _badges = MutableStateFlow(initialBadges())
    val badges: StateFlow<List<Badge>> = _badges.asStateFlow()

    init {
        refreshBadges()
    }

    fun refreshBadges() {
        viewModelScope.launch {
            val records = withContext(Dispatchers.IO) { database.stepDao().getAllRecords() }
            val settings = preferences.loadSettings()

            val totalSteps = records.sumOf { it.stepCount }
            val totalDistanceKm = records.sumOf { it.distanceKilometers }
            val totalFloors = records.sumOf { it.floorsAscended }
            val maxSingleDaySteps = records.maxOfOrNull { it.stepCount } ?: 0

            var streak = 0
            var maxStreak = 0
            records.sortedBy { it.date }.forEach { r ->
                if (r.stepCount >= settings.stepGoal) {
                    streak++
                    if (streak > maxStreak) maxStreak = streak
                } else {
                    streak = 0
                }
            }

            val updated = initialBadges().map { badge ->
                val progress: Double = when (badge.id) {
                    "streak_3", "streak_7", "streak_30" -> maxStreak.toDouble()
                    "step_10k", "step_15k", "step_20k" -> maxSingleDaySteps.toDouble()
                    "dist_marathon", "dist_100k" -> totalDistanceKm
                    "climb_tower", "climb_everest" -> totalFloors.toDouble()
                    else -> 0.0
                }
                badge.copy(
                    currentProgress = progress,
                    isUnlocked = progress >= badge.requirement
                )
            }
            _badges.value = updated
        }
    }

    private fun initialBadges(): List<Badge> {
        return listOf(
            Badge("streak_3", "3-Day Streak", "Hit your daily step goal 3 days in a row", BadgeCategory.STREAK, 3.0),
            Badge("streak_7", "7-Day Warrior", "Hit your daily step goal 7 days in a row", BadgeCategory.STREAK, 7.0),
            Badge("streak_30", "Monthly Master", "Hit your daily step goal 30 days in a row", BadgeCategory.STREAK, 30.0),

            Badge("step_10k", "10K Club", "Walk 10,000 steps in a single day", BadgeCategory.DAILY_STEPS, 10000.0),
            Badge("step_15k", "15K Explorer", "Walk 15,000 steps in a single day", BadgeCategory.DAILY_STEPS, 15000.0),
            Badge("step_20k", "20K Ultra", "Walk 20,000 steps in a single day", BadgeCategory.DAILY_STEPS, 20000.0),

            Badge("dist_marathon", "Marathon", "Cover a total distance of 42.195 km", BadgeCategory.LIFETIME_DISTANCE, 42.2),
            Badge("dist_100k", "Centurion (100 km)", "Cover 100 kilometers on foot", BadgeCategory.LIFETIME_DISTANCE, 100.0),

            Badge("climb_tower", "Tower Climber", "Climb 50 floors in total", BadgeCategory.ELEVATION, 50.0),
            Badge("climb_everest", "Mount Everest", "Climb 1,000 floors of elevation", BadgeCategory.ELEVATION, 1000.0)
        )
    }
}
