package com.pedometer.app.data.model

enum class AppThemeColor(val title: String, val hexColor: Long) {
    GREEN("Classic Green", 0xFF26D07C),
    ORANGE("Sunset Orange", 0xFFFF8C00),
    BLUE("Electric Blue", 0xFF00A3FF),
    PURPLE("Neon Purple", 0xFFBB53FE),
    RED("Fire Red", 0xFFFF3B30)
}

enum class ColorMode(val title: String) {
    SYSTEM("System Default"),
    DARK("Dark Mode"),
    LIGHT("Light Mode")
}

data class AppSettings(
    val stepGoal: Int = 10000,
    val floorGoal: Int = 10,
    val useMetricUnits: Boolean = true,
    val themeColor: AppThemeColor = AppThemeColor.GREEN,
    val colorMode: ColorMode = ColorMode.DARK,
    val enableNotifications: Boolean = true,
    val strideLengthCm: Double = 75.0
)
