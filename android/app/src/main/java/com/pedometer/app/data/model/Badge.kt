package com.pedometer.app.data.model

enum class BadgeCategory(val title: String) {
    STREAK("Streaks"),
    DAILY_STEPS("Daily Records"),
    LIFETIME_DISTANCE("Lifetime Distance"),
    ELEVATION("Climbing")
}

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val category: BadgeCategory,
    val requirement: Double,
    val currentProgress: Double = 0.0,
    val isUnlocked: Boolean = false
) {
    val progressRatio: Float
        get() = if (requirement > 0) (currentProgress / requirement).toFloat().coerceIn(0f, 1f) else 0f
}
