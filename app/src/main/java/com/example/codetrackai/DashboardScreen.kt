package com.example.codetrackai

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: DashboardViewModel) {
    LaunchedEffect(Unit) {
        viewModel.loadDataFromFirebase()
    }

    var showAiSheet by remember { mutableStateOf(false) }
    var userQuery by remember { mutableStateOf("") }
    val geminiApiKey = BuildConfig.GEMINI_API_KEY

    // Progress list calculations directly linked to actual snapshot values
    val progressList = listOf(
        viewModel.totalLeetCodeSolved.toFloat(),
        viewModel.totalCodeforcesSolved.toFloat(),
        viewModel.problemsSolved.toFloat()
    )

    Scaffold(
        containerColor = Color.Black,
        floatingActionButton = {
            Button(
                onClick = { showAiSheet = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Get AI Help 🤖✨", color = Color.White)
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 30.dp, bottom = 80.dp)
        ) {

            item {
                Text(
                    text = "Welcome, ${viewModel.userName} 🚀",
                    fontSize = 28.sp,
                    color = Color(0xFF2196F3),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Problems Solved: ${viewModel.problemsSolved} 💻", color = Color.White, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(5.dp))
                        Text("LeetCode: ${viewModel.totalLeetCodeSolved} | Codeforces: ${viewModel.totalCodeforcesSolved}", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Overall Progress History", fontSize = 16.sp, color = Color(0xFF2196F3))
                        Spacer(modifier = Modifier.height(15.dp))

                        val totalSum = progressList.sum()
                        if (totalSum > 0f) {
                            Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
                                val max = progressList.maxOrNull() ?: 1f
                                val safeMax = if (max == 0f) 1f else max
                                val width = size.width
                                val height = size.height
                                val stepX = width / (progressList.size - 1)

                                for (i in 0 until progressList.size - 1) {
                                    val x1 = i * stepX
                                    val y1 = height - (progressList[i] / safeMax) * height
                                    val x2 = (i + 1) * stepX
                                    val y2 = height - (progressList[i + 1] / safeMax) * height

                                    drawLine(
                                        color = Color(0xFF2196F3),
                                        start = Offset(x1, y1),
                                        end = Offset(x2, y2),
                                        strokeWidth = 6f
                                    )
                                }
                            }
                        } else {
                            Text("Solve some problems to view progress 📈", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { navController.navigate("tasks") },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Open Tasks ✅", color = Color.White)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 🌟 NEW: View Profile Component Added
            item {
                Button(
                    onClick = { navController.navigate("profile") },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA116)) // LeetCode Premium Orange
                ) {
                    Text("View LeetCode Profile 👤⚡", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                Button(
                    onClick = {
                        viewModel.saveDataToFirebase()
                        viewModel.logout {
                            navController.navigate("login") {
                                popUpTo("dashboard") { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout 🚪", color = Color.White)
                }
            }
        }
    }

    // 🛠️ FIXED AI SHEET: Pure components UI structure restore ki hai yahan
    // 🛠️ FIXED AI SHEET: Generic prompt support + Smooth Scroll added
    if (showAiSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAiSheet = false },
            containerColor = Color(0xFF1A1A1A),
            modifier = Modifier.fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                Text("CodeTrackAI Assistant 🤖", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(15.dp))

                OutlinedTextField(
                    value = userQuery,
                    onValueChange = { userQuery = it },
                    label = { Text("Ask anything...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(15.dp))

                Button(
                    onClick = {
                        if (userQuery.isNotBlank()) {
                            viewModel.askGeminiAssistant(userQuery, geminiApiKey)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Send Message", color = Color.White)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("Response:", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(5.dp))

                // 🚀 FIX: Is dynamic LazyColumn container ki wajah se text kitna bhi bada ho, perfect scroll hoga
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF262626))
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        item {
                            SelectionContainer {
                                when (val state = viewModel.aiState) {
                                    is AiState.Loading -> Text("Processing Response...", color = Color.Yellow, fontSize = 16.sp)
                                    is AiState.Success -> Text(state.response, color = Color.White, fontSize = 16.sp)
                                    is AiState.Error -> Text("Error: ${state.error}", color = Color.Red, fontSize = 14.sp)
                                    else -> Text("Ask me any query about code, logic, errors, or frameworks!", color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}