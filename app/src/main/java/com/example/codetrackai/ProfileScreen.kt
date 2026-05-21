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

                // Sync action trigger mechanism
                item {
                    Spacer(modifier = Modifier.height(15.dp))
                    Button(
                        onClick = { viewModel.fetchProfileScreenDetails() },
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