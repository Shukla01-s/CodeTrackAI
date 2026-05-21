package com.example.codetrackai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: DashboardViewModel) {
    LaunchedEffect(Unit) {
        viewModel.loadDataFromFirebase()
    }

    var showAiSheet by remember { mutableStateOf(false) }
    var userQuery by remember { mutableStateOf("") }
    val geminiApiKey = BuildConfig.GEMINI_API_KEY

    Scaffold(
        containerColor = Color.Black
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 40.dp, bottom = 40.dp)
        ) {

            // Sleek Header Block with Top Right AI Help Trigger
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Welcome back,",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${viewModel.userName} 🚀",
                            fontSize = 26.sp,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Button(
                        onClick = { showAiSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Text("AI Help 🤖", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Minimalist Problems Solved Status Counter Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222222))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Overall Metrics",
                            color = Color(0xFF2196F3),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Solved:",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${viewModel.problemsSolved} 💻",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color(0xFF222222), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("LeetCode", color = Color(0xFFFFA116), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("${viewModel.totalLeetCodeSolved} Qns", color = Color.LightGray, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Codeforces", color = Color(0xFF2196F3), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("${viewModel.totalCodeforcesSolved} Qns", color = Color.LightGray, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Action Dashboard Controls Panel
            item {
                Button(
                    onClick = { navController.navigate("tasks") },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Open Tasks ✅", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            item {
                Button(
                    onClick = { navController.navigate("profile") },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA116)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("View Coding Profile 👤⚡", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 🌟 DYNAMIC MATRIX INTEGRATION: Custom LeetCode Style Real Dynamic App Streak Dashboard Grid
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F141C)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "CodeTrackAI App Streak",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Consistent platform activity matrix",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }

                            // Dynamic Badge Linked directly to backend counter
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("🔥", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${viewModel.appStreakCount} Days",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Real Dynamic Matrix Grid Calculations (Last 36 Days Viewport Layout)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val gridCalendar = Calendar.getInstance()
                            gridCalendar.add(Calendar.DAY_OF_YEAR, -35) // Dynamic sliding window track point
                            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                            for (col in 0 until 9) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    for (row in 0 until 4) {
                                        val calculatedDateStr = formatter.format(gridCalendar.time)
                                        val isUserActiveOnDate = viewModel.activeDatesList.contains(calculatedDateStr)

                                        val blockColor = if (isUserActiveOnDate) {
                                            Color(0xFF26A641) // Highlight active dynamic real green
                                        } else {
                                            Color(0xFF161B22) // Standard background container cell
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .background(blockColor, RoundedCornerShape(3.dp))
                                        )
                                        gridCalendar.add(Calendar.DAY_OF_YEAR, 1)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Less ", color = Color.Gray, fontSize = 10.sp)
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFF161B22), RoundedCornerShape(2.dp)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFF26A641), RoundedCornerShape(2.dp)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(" More", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Logout Action Trigger Button
            item {
                OutlinedButton(
                    onClick = {
                        viewModel.saveDataToFirebase()
                        viewModel.logout {
                            navController.navigate("login") {
                                popUpTo("dashboard") { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Logout 🚪", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // AI Sheet Overlay
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