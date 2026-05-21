package com.example.codetrackai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(navController: NavController, auth: FirebaseAuth, db: FirebaseFirestore) {
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var leetcode by remember { mutableStateOf("") }
    var codeforces by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var isFetchingOldData by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val userId = auth.currentUser?.uid

    // 🌟 NEW: Check unique dynamic backstack route to determine navigation context
    val isEditingFromProfile = remember {
        navController.previousBackStackEntry?.destination?.route == "profile"
    }

    // 🌟 NEW: Pre-fill existing data from Firebase if user is editing their profile handles
    LaunchedEffect(userId) {
        if (userId != null) {
            try {
                val document = db.collection("users").document(userId).get().await()
                if (document.exists()) {
                    name = document.getString("name") ?: ""
                    mobile = document.getString("mobile") ?: ""
                    leetcode = document.getString("leetcodeHandle") ?: document.getString("leetcode") ?: ""
                    codeforces = document.getString("codeforcesHandle") ?: document.getString("codeforces") ?: ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isFetchingOldData = false
            }
        } else {
            isFetchingOldData = false
        }
    }

    fun sanitizeUsername(input: String): String {
        return input.trim().substringAfterLast("/").substringBefore("?")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)) // Matches Premium LeetCode Dark Base
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isFetchingOldData) {
            CircularProgressIndicator(color = Color(0xFFFFA116))
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isEditingFromProfile) "Update Your Profile ⚙️" else "Complete Your Profile ✍️",
                    fontSize = 26.sp,
                    color = Color(0xFFFFA116), // Styled Premium Accent Color
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isEditingFromProfile) "Modify your workspace tracking configs" else "Setup your details for personalized tracking",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFFA116), focusedLabelColor = Color(0xFFFFA116),
                        unfocusedBorderColor = Color(0xFF333333)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { if (it.length <= 10) mobile = it },
                    label = { Text("Mobile Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFFA116), focusedLabelColor = Color(0xFFFFA116),
                        unfocusedBorderColor = Color(0xFF333333)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = leetcode,
                    onValueChange = { leetcode = it },
                    label = { Text("LeetCode Username (Optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFFB300), focusedLabelColor = Color(0xFFFFB300),
                        unfocusedBorderColor = Color(0xFF333333)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = codeforces,
                    onValueChange = { codeforces = it },
                    label = { Text("Codeforces Username (Optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF2196F3), focusedLabelColor = Color(0xFF2196F3),
                        unfocusedBorderColor = Color(0xFF333333)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(24.dp))

                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(bottom = 12.dp))
                }

                Button(
                    onClick = {
                        if (name.trim().isEmpty() || mobile.trim().isEmpty()) {
                            errorMessage = "Please fill Name and Mobile Number!"
                            return@Button
                        }
                        if (mobile.length < 10) {
                            errorMessage = "Enter a valid 10-digit mobile number!"
                            return@Button
                        }

                        isLoading = true
                        errorMessage = ""

                        coroutineScope.launch {
                            try {
                                val userEmail = auth.currentUser?.email

                                if (userId != null) {
                                    // 🛠️ FIX: Keys matched exactly with UserEntity & ViewModel cache structures
                                    val userProfile = mapOf(
                                        "name" to name.trim(),
                                        "mobile" to mobile.trim(),
                                        "email" to userEmail,
                                        "leetcodeHandle" to sanitizeUsername(leetcode),
                                        "codeforcesHandle" to sanitizeUsername(codeforces)
                                    )

                                    db.collection("users").document(userId)
                                        .set(userProfile, SetOptions.merge())
                                        .await()

                                    isLoading = false

                                    // 🌟 FIX: Smart target routing context logic
                                    if (isEditingFromProfile) {
                                        navController.popBackStack() // Smooth reverse transaction directly back to Profile
                                    } else {
                                        navController.navigate("dashboard") {
                                            popUpTo("profileSetup") { inclusive = true }
                                        }
                                    }
                                } else {
                                    isLoading = false
                                    errorMessage = "User session expired. Please Login again."
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = "Database Error: ${e.localizedMessage ?: e.message}"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA116)), // Accent Match
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (isEditingFromProfile) "Save Changes 💾" else "Save & Continue ➡️",
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}