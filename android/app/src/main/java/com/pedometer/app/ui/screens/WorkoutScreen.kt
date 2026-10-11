package com.pedometer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedometer.app.data.model.WorkoutType
import com.pedometer.app.ui.components.MetricCard
import com.pedometer.app.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(viewModel: WorkoutViewModel) {
    val isWorkoutActive by viewModel.isWorkoutActive.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val currentWorkout by viewModel.currentWorkout.collectAsState()
    val pastWorkouts by viewModel.pastWorkouts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GPS Workout", fontWeight = FontWeight.Bold) },
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
            // Live Status Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = when (selectedType) {
                                    WorkoutType.RUN -> Icons.AutoMirrored.Filled.DirectionsRun
                                    WorkoutType.HIKE -> Icons.Default.Hiking
                                    else -> Icons.AutoMirrored.Filled.DirectionsWalk
                                },
                                contentDescription = null,
                                tint = Color(0xFF26D07C),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isWorkoutActive) "GPS TRACKING ACTIVE (${currentWorkout.routePoints.size} PTS)" else "READY FOR OUTDOOR WORKOUT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isWorkoutActive) Color(0xFF26D07C) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Type Selector
            if (!isWorkoutActive) {
                item {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        WorkoutType.values().forEachIndexed { index, type ->
                            SegmentedButton(
                                selected = selectedType == type,
                                onClick = { viewModel.setWorkoutType(type) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = WorkoutType.values().size)
                            ) {
                                Text(type.title)
                            }
                        }
                    }
                }
            }

            // Timer & Pace Display
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentWorkout.formattedDuration,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "DURATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Metric Cards
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        MetricCard(
                            icon = Icons.AutoMirrored.Filled.AltRoute,
                            title = "Distance",
                            value = String.format("%.2f km", currentWorkout.distanceMeters / 1000.0),
                            tintColor = Color(0xFF26D07C)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        MetricCard(
                            icon = Icons.Default.Speed,
                            title = "Avg Pace",
                            value = currentWorkout.formattedPace,
                            tintColor = Color(0xFF00A3FF)
                        )
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        MetricCard(
                            icon = Icons.Default.LocalFireDepartment,
                            title = "Burned",
                            value = "${currentWorkout.caloriesBurned.toInt()} kcal",
                            tintColor = Color(0xFFFF8C00)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        MetricCard(
                            icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                            title = "Steps",
                            value = "${currentWorkout.stepCount}",
                            tintColor = Color(0xFFBB53FE)
                        )
                    }
                }
            }

            // Control Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isWorkoutActive) {
                        Button(
                            onClick = { viewModel.startWorkout() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26D07C))
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("START WORKOUT", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    } else {
                        IconButton(
                            onClick = { if (isPaused) viewModel.resumeWorkout() else viewModel.pauseWorkout() },
                            modifier = Modifier.size(68.dp).background(Color(0xFFFF8C00), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        IconButton(
                            onClick = { viewModel.stopWorkout() },
                            modifier = Modifier.size(68.dp).background(Color(0xFFFF3B30), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            // Past Workouts
            if (pastWorkouts.isNotEmpty()) {
                item {
                    Text(
                        text = "RECENT WORKOUTS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                items(pastWorkouts) { past ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(
                                    modifier = Modifier.size(40.dp).background(Color(0xFF26D07C).copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, tint = Color(0xFF26D07C))
                                }
                                Column {
                                    Text(past.type.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    val df = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                                    Text(df.format(Date(past.startTimestamp)), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(String.format("%.2f km", past.distanceMeters / 1000.0), fontWeight = FontWeight.Black, fontSize = 15.sp)
                                Text(past.formattedDuration, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
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
