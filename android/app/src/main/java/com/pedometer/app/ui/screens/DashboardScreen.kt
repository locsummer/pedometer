package com.pedometer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedometer.app.ui.components.DailyBarChart
import com.pedometer.app.ui.components.MetricCard
import com.pedometer.app.ui.components.StepRingGauge
import com.pedometer.app.viewmodel.MainViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val currentRecord by viewModel.currentRecord.collectAsState()
    val recentRecords by viewModel.recentRecords.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val streak by viewModel.currentStreak.collectAsState()

    val accentColor = Color(settings.themeColor.hexColor)
    val isToday = selectedDate == LocalDate.now().toString()
    val last7Days = viewModel.getLast7Days(recentRecords)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pedometer++",
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Date + Streak
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isToday) "Today" else selectedDate,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!isToday) {
                            Text(
                                text = "Back to Today",
                                fontSize = 12.sp,
                                color = accentColor,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { viewModel.selectToday() }
                            )
                        }
                    }

                    // Streak Pill
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFFF8C00).copy(alpha = 0.15f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFFF8C00),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "$streak Days",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF8C00)
                        )
                    }
                }
            }

            // Circular Step Ring Gauge
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    StepRingGauge(
                        currentSteps = currentRecord.stepCount,
                        goalSteps = settings.stepGoal,
                        accentColor = accentColor
                    )
                }
            }

            // 4 Grid Metric Cards
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        MetricCard(
                            icon = Icons.Default.Navigation,
                            title = "Distance",
                            value = currentRecord.formattedDistance(settings.useMetricUnits),
                            tintColor = Color(0xFF00A3FF)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        MetricCard(
                            icon = Icons.Default.Stairs,
                            title = "Floors",
                            value = "${currentRecord.floorsAscended} Floors",
                            tintColor = Color(0xFFBB53FE)
                        )
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        MetricCard(
                            icon = Icons.Default.LocalFireDepartment,
                            title = "Active Cal",
                            value = "${currentRecord.activeCalories.toInt()} kcal",
                            tintColor = Color(0xFFFF8C00)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        MetricCard(
                            icon = Icons.Default.Schedule,
                            title = "Active Time",
                            value = "${currentRecord.activeMinutes} mins",
                            tintColor = Color(0xFF26D07C)
                        )
                    }
                }
            }

            // 7-Day Bar Chart
            item {
                DailyBarChart(
                    records = last7Days,
                    goalSteps = settings.stepGoal,
                    selectedDate = selectedDate,
                    accentColor = accentColor,
                    onSelectDate = { viewModel.selectDate(it) }
                )
            }

            // Hourly Histogram
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "HOURLY ACTIVITY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            letterSpacing = 0.5.sp
                        )

                        val maxHourly = (currentRecord.hourlySteps.maxOrNull() ?: 1).coerceAtLeast(1)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(65.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            currentRecord.hourlySteps.forEach { count ->
                                val fraction = (count.toFloat() / maxHourly.toFloat()).coerceIn(0.04f, 1f)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(fraction)
                                        .padding(horizontal = 1.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (count > 0) accentColor else Color.Gray.copy(alpha = 0.2f))
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("12 AM", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text("12 PM", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text("11 PM", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
