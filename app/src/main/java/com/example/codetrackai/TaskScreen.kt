package com.example.codetrackai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    navController: NavController,
    viewModel: DashboardViewModel
) {
    val tasks = viewModel.tasks
    val progress = viewModel.getTaskProgress()
    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Coding Tasks 📝", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A1A)),
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("◀ Back", color = Color(0xFFFFA116), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            )
        },
        containerColor = Color(0xFF0A0A0A) // Clean LeetCode Dark Base
    ) { paddingValues ->

        // 🚀 LazyColumn lagaya taaki scroll performant rahe aur components overlap na hon
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🌟 1. PREMIUM LEETCODE PROGRESS CARD (Circular Style Ring metrics)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Session Progress", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$completedCount / $totalCount Done",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // Circular Tracker Indicator segment
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { if (totalCount == 0) 0f else progress },
                                modifier = Modifier.size(70.dp),
                                color = Color(0xFF2DB55D), // LeetCode Accepted Green
                                strokeWidth = 6.dp,
                                trackColor = Color(0xFF3A3A3A)
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 🌟 2. SEAMLESS COMPONENT INPUT ROW
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = viewModel.taskInput,
                        onValueChange = { viewModel.taskInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Add a new coding checkpoint...", color = Color.Gray) },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1E1E1E),
                            unfocusedContainerColor = Color(0xFF1E1E1E),
                            focusedBorderColor = Color(0xFFFFA116), // Premium Orange Highlight
                            unfocusedBorderColor = Color(0xFF333333)
                        )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            if (viewModel.taskInput.isNotBlank()) {
                                viewModel.addTask()
                                viewModel.saveDataToFirebase()
                            }
                        },
                        modifier = Modifier.height(54.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA116)) // Orange accent callout
                    ) {
                        Text("+ Add", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
                Spacer(modifier = Modifier.height(15.dp))
            }

            // 🌟 3. PREMIUM CARD-LIST ITEMS
            if (tasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No tasks active. Start pinning checkmarks! 🚀", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                itemsIndexed(tasks) { index, task ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (task.isCompleted) Color(0xFF141414) else Color(0xFF1E1E1E)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = {
                                    viewModel.toggleTask(index)
                                    viewModel.saveDataToFirebase()
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF2DB55D),
                                    uncheckedColor = Color.Gray,
                                    checkmarkColor = Color.Black
                                )
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = task.title,
                                color = if (task.isCompleted) Color.DarkGray else Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            )
                        }
                    }
                }
            }
        }
    }
}