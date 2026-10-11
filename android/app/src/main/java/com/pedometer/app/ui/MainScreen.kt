package com.pedometer.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pedometer.app.ui.screens.*
import com.pedometer.app.ui.theme.PedometerTheme
import com.pedometer.app.viewmodel.*

enum class AppTab(val title: String, val icon: ImageVector) {
    TODAY("Today", Icons.AutoMirrored.Filled.DirectionsWalk),
    WORKOUT("Workouts", Icons.AutoMirrored.Filled.DirectionsRun),
    TRENDS("Trends", Icons.Default.BarChart),
    BADGES("Badges", Icons.Default.EmojiEvents),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun MainScreen(
    mainViewModel: MainViewModel = viewModel(),
    workoutViewModel: WorkoutViewModel = viewModel(),
    badgesViewModel: BadgesViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settings by settingsViewModel.settings.collectAsState()
    var selectedTab by remember { mutableStateOf(AppTab.TODAY) }
    val accentColor = Color(settings.themeColor.hexColor)

    PedometerTheme(
        accentColor = accentColor,
        colorMode = settings.colorMode
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    AppTab.values().forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = accentColor,
                                selectedTextColor = accentColor,
                                indicatorColor = accentColor.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier.padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                when (selectedTab) {
                    AppTab.TODAY -> DashboardScreen(mainViewModel)
                    AppTab.WORKOUT -> WorkoutScreen(workoutViewModel)
                    AppTab.TRENDS -> TrendsScreen(mainViewModel)
                    AppTab.BADGES -> BadgesScreen(badgesViewModel)
                    AppTab.SETTINGS -> SettingsScreen(settingsViewModel)
                }
            }
        }
    }
}
