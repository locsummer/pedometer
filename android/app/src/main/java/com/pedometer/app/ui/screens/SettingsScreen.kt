package com.pedometer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedometer.app.data.model.AppThemeColor
import com.pedometer.app.data.model.ColorMode
import com.pedometer.app.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(
                    text = "DAILY TARGETS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Step Goal", fontWeight = FontWeight.Bold)
                                Text("${settings.stepGoal} steps", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { if (settings.stepGoal > 1000) viewModel.updateSettings(settings.copy(stepGoal = settings.stepGoal - 500)) },
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) { Text("-500") }
                                Button(
                                    onClick = { viewModel.updateSettings(settings.copy(stepGoal = settings.stepGoal + 500)) },
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) { Text("+500") }
                            }
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Floor Goal", fontWeight = FontWeight.Bold)
                                Text("${settings.floorGoal} flights", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { if (settings.floorGoal > 1) viewModel.updateSettings(settings.copy(floorGoal = settings.floorGoal - 1)) },
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) { Text("-1") }
                                Button(
                                    onClick = { viewModel.updateSettings(settings.copy(floorGoal = settings.floorGoal + 1)) },
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) { Text("+1") }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "APPEARANCE & THEME",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Theme Accent Color", fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            AppThemeColor.values().forEach { theme ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(theme.hexColor), CircleShape)
                                        .then(
                                            if (settings.themeColor == theme) Modifier.padding(3.dp)
                                            else Modifier
                                        )
                                ) {
                                    IconButton(onClick = { viewModel.updateSettings(settings.copy(themeColor = theme)) }) {}
                                }
                            }
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Color Mode", fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ColorMode.values().forEach { mode ->
                                    FilterChip(
                                        selected = settings.colorMode == mode,
                                        onClick = { viewModel.updateSettings(settings.copy(colorMode = mode)) },
                                        label = { Text(mode.title.take(4)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "UNITS & SENSORS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Use Metric Units (km)", fontWeight = FontWeight.Bold)
                            Switch(
                                checked = settings.useMetricUnits,
                                onCheckedChange = { viewModel.updateSettings(settings.copy(useMetricUnits = it)) }
                            )
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ongoing Notification", fontWeight = FontWeight.Bold)
                            Switch(
                                checked = settings.enableNotifications,
                                onCheckedChange = { viewModel.updateSettings(settings.copy(enableNotifications = it)) }
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { viewModel.resetDefaults() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset Settings to Default")
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
