package com.pedometer.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun StepRingGauge(
    currentSteps: Int,
    goalSteps: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val progress = (currentSteps.toFloat() / goalSteps.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "RingProgress"
    )

    val isGoalReached = currentSteps >= goalSteps
    val numberFormatter = NumberFormat.getNumberInstance(Locale.US)

    Box(
        modifier = modifier
            .size(240.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 22.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = Size(diameter, diameter)

            // Background Track Ring
            drawArc(
                color = Color.Gray.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Foreground Progress Arc
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(accentColor.copy(alpha = 0.7f), accentColor, accentColor)
                ),
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Inside text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isGoalReached) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.DirectionsWalk,
                contentDescription = null,
                tint = if (isGoalReached) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = numberFormatter.format(currentSteps),
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "GOAL: ${numberFormatter.format(goalSteps)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                letterSpacing = 0.5.sp
            )

            if (isGoalReached) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .background(accentColor.copy(alpha = 0.18f), shape = CircleShape)
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${(currentSteps * 100 / goalSteps)}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                }
            }
        }
    }
}
