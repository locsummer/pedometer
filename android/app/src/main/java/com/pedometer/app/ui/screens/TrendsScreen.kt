package com.pedometer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
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
import com.pedometer.app.ui.components.MetricCard
import com.pedometer.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsScreen(viewModel: MainViewModel) {
    val recentRecords by viewModel.recentRecords.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val averageSteps = if (recentRecords.isNotEmpty()) recentRecords.map { it.stepCount }.average().toInt() else 0
    val totalDistance = recentRecords.sumOf { it.distanceKilometers }
    val totalFloors = recentRecords.sumOf { it.floorsAscended }
    val bestDay = recentRecords.maxByOrNull { it.stepCount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trends & Stats", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "LIFETIME TOTALS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    letterSpacing = 0.5.sp
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        MetricCard(
                            icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                            title = "Daily Average",
                            value = "$averageSteps",
                            subtitle = "steps / day",
                            tintColor = Color(0xFF26D07C)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        MetricCard(
                            icon = Icons.Default.Map,
                            title = "Total Distance",
                            value = String.format("%.1f km", totalDistance),
                            subtitle = "walked & run",
                            tintColor = Color(0xFF00A3FF)
                        )
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        MetricCard(
                            icon = Icons.Default.Apartment,
                            title = "Total Floors",
                            value = "$totalFloors",
                            subtitle = "flights climbed",
                            tintColor = Color(0xFFBB53FE)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        MetricCard(
                            icon = Icons.Default.EmojiEvents,
                            title = "Best Day",
                            value = "${bestDay?.stepCount ?: 0}",
                            subtitle = bestDay?.date ?: "-",
                            tintColor = Color(0xFFFF8C00)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "30-DAY STEP VISUALIZER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        val maxSteps = maxOf(settings.stepGoal, recentRecords.maxOfOrNull { it.stepCount } ?: 10000)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            recentRecords.take(30).reversed().forEach { rec ->
                                val isMet = rec.stepCount >= settings.stepGoal
                                val fraction = (rec.stepCount.toFloat() / maxSteps.toFloat()).coerceIn(0.04f, 1f)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(fraction)
                                        .padding(horizontal = 1.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (isMet) Color(0xFF26D07C) else Color(0xFFFF8C00))
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("30 days ago", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text("Today", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
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
