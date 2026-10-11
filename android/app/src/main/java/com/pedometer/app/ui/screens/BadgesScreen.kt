package com.pedometer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedometer.app.data.model.Badge
import com.pedometer.app.data.model.BadgeCategory
import com.pedometer.app.viewmodel.BadgesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesScreen(viewModel: BadgesViewModel) {
    val badges by viewModel.badges.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BadgeCategory.values().forEach { category ->
                val categoryBadges = badges.filter { it.category == category }
                if (categoryBadges.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text = category.title.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(categoryBadges) { badge ->
                        BadgeItemCard(badge = badge)
                    }
                }
            }

            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun BadgeItemCard(badge: Badge) {
    val cardAlpha = if (badge.isUnlocked) 1f else 0.65f

    Card(
        modifier = Modifier.fillMaxWidth().height(170.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = cardAlpha))
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(
                        if (badge.isUnlocked) Color(0xFFFF8C00).copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f),
                        CircleShape
                    )
                    .then(
                        if (badge.isUnlocked) Modifier.border(2.dp, Color(0xFFFF8C00), CircleShape)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = if (badge.isUnlocked) Color(0xFFFF8C00) else Color.Gray.copy(alpha = 0.4f),
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = badge.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = badge.description,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }

            if (badge.isUnlocked) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFF8C00).copy(alpha = 0.15f), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("UNLOCKED", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF8C00))
                }
            } else {
                LinearProgressIndicator(
                    progress = { badge.progressRatio },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = Color(0xFFFF8C00),
                    trackColor = Color.Gray.copy(alpha = 0.2f)
                )
            }
        }
    }
}
