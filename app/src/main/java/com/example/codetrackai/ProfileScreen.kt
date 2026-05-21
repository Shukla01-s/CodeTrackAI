package com.example.codetrackai

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: DashboardViewModel) {

    // Auto pull metrics configuration on load
    LaunchedEffect(Unit) {
        viewModel.fetchProfileScreenDetails()
        // 🌟 STREAK TRIGGER ADD-ON: LeetCode streak calculation ko background mein trigger karega
        viewModel.syncLeetCodeStreakOnly()
        // 🌟 AI ANALYTICS TRIGGER: Screen load hote hi background call initiate karega
        viewModel.fetchAiTopicInsights(apiKey = BuildConfig.GEMINI_API_KEY)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Coding Profile ⚡", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A1A)),
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("◀ Back", color = Color(0xFFFFA116), fontWeight = FontWeight.Bold)
                    }
                },
                // 🌟 FIX: Right upper corner mein Settings/Edit icon (⚙️) inject kar diya
                actions = {
                    IconButton(onClick = { navController.navigate("profileSetup") }) {
                        Text(
                            text = "⚙️",
                            fontSize = 22.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFF0A0A0A)
    ) { paddingValues ->
        if (viewModel.isProfileLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFFA116))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 20.dp)
            ) {
                // Top Header block
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Coder: ${viewModel.userName} 🔥", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "LeetCode ID: ${viewModel.leetcodeHandle.ifEmpty { "Not Added" }}", color = Color.LightGray, fontSize = 14.sp)
                            Text(text = "Codeforces ID: ${viewModel.codeforcesHandle.ifEmpty { "Not Added" }}", color = Color.LightGray, fontSize = 14.sp)
                        }
                    }
                }

                // 🌟 NEW FEATURE ADD-ON: Isolated LeetCode Streak UI Display
                if (viewModel.leetcodeHandle.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1D11)), // Slight Orange/Brown hint for fire theme
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("LeetCode Streak Counter", color = Color(0xFFFFA116), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (viewModel.lastLeetCodeSolvedDate.isNotEmpty()) "Last Active: ${viewModel.lastLeetCodeSolvedDate}" else "No recent activity tracked",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🔥", fontSize = 28.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${viewModel.leetcodeStreak} Days",
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }

                // 🌟 NEW FEATURE ADD-ON: Platform Questions Distribution Breakdown Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Platform Distribution Breakdown 📊", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("LeetCode Solved", color = Color(0xFFFFA116), fontSize = 13.sp)
                                    Text("${viewModel.totalLeetCodeSolved} Qns", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Codeforces Solved", color = Color(0xFF3B82F6), fontSize = 13.sp)
                                    Text("${viewModel.totalCodeforcesSolved} Qns", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Custom Computed Rating Display Metric Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF242424)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("CodeTrackAI Rating Index", color = Color(0xFFFFA116), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text(
                                text = "${viewModel.calculatedRating}",
                                fontSize = 42.sp,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text("Rank Level: ${viewModel.cfRank.replaceFirstChar { it.uppercase() }}", color = Color.Green, fontSize = 14.sp)
                        }
                    }
                }

                // Continuous Track History Analytics Graph Canvas
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Personal Rating Evaluation Trend", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(20.dp))

                            if (viewModel.ratingHistory.isNotEmpty()) {
                                Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                                    val maxVal = viewModel.ratingHistory.maxOrNull() ?: 1f
                                    val minVal = viewModel.ratingHistory.minOrNull() ?: 0f
                                    val diff = if (maxVal - minVal == 0f) 1f else maxVal - minVal

                                    val width = size.width
                                    val height = size.height
                                    val stepX = width / (viewModel.ratingHistory.size - 1)

                                    for (i in 0 until viewModel.ratingHistory.size - 1) {
                                        val x1 = i * stepX
                                        val y1 = height - ((viewModel.ratingHistory[i] - minVal) / diff) * height
                                        val x2 = (i + 1) * stepX
                                        val y2 = height - ((viewModel.ratingHistory[i + 1] - minVal) / diff) * height

                                        drawLine(
                                            color = Color(0xFFFFA116),
                                            start = Offset(x1, y1),
                                            end = Offset(x2, y2),
                                            strokeWidth = 8f
                                        )
                                    }
                                }
                            } else {
                                Text("No progression checkpoint calculated yet.", color = Color.Gray)
                            }
                        }
                    }
                }

                // 🌟 NEW FEATURE ADD-ON: AI Weak/Strong Insights Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Topic Insights (Gemini AI Analyzer) 🧠", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Strong Topics Row Mapping
                            Text("⚡ Strong Areas:", color = Color.Green, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))

                            val strongList = viewModel.strongTopics
                            if (strongList.isNotEmpty()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    strongList.forEach { topic ->
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text(topic, color = Color.White, fontSize = 11.sp) },
                                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF1B3B2B))
                                        )
                                    }
                                }
                            } else {
                                Text("Analyzing profiles to extract skills...", color = Color.Gray, fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Weak Topics Row Mapping
                            Text("⚠️ Focus Needed:", color = Color(0xFFEF4444), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))

                            val weakList = viewModel.weakTopics
                            if (weakList.isNotEmpty()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    weakList.forEach { topic ->
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text(topic, color = Color.White, fontSize = 11.sp) },
                                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF4A1D1D))
                                        )
                                    }
                                }
                            } else {
                                Text("Determining improvement categories...", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Sync action trigger mechanism
                item {
                    Spacer(modifier = Modifier.height(15.dp))
                    Button(
                        onClick = {
                            viewModel.fetchProfileScreenDetails()
                            // 🌟 STREAK TRIGGER ALSO ON SYNC BUTTON CLICK
                            viewModel.syncLeetCodeStreakOnly()
                            // 🌟 AI ANALYTICS RE-TRIGGER ON LIVE BUTTON SYNC
                            viewModel.fetchAiTopicInsights(apiKey = BuildConfig.GEMINI_API_KEY)
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA116)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Fetch Live Platform Ratings 🔄", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}