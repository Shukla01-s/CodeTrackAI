package com.example.codetrackai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, auth: FirebaseAuth, db: FirebaseFirestore) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Dark Theme Consistent Color
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo / Title
            Text(
                text = "Welcome Back 👋",
                fontSize = 28.sp,
                color = Color(0xFF2196F3),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Login to track your coding progress",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Email Input Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3)
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Password Input Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3)
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Error Message (Agar galat details ho)
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Login Button
            // Login Button ka safe and non-freezing click logic
            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        errorMessage = "Bhai, Email aur Password fill karo!"
                        return@Button
                    }
                    isLoading = true
                    errorMessage = ""

                    // Safe background authentication call
                    auth.signInWithEmailAndPassword(email.trim(), password.trim())
                        .addOnSuccessListener { authResult ->
                            val userId = authResult.user?.uid
                            if (userId != null) {
                                // 🚀 Firebase fetch ko bilkul safe block me dal diya
                                db.collection("users").document(userId).get()
                                    .addOnCompleteListener { task ->
                                        isLoading = false
                                        if (task.isSuccessful) {
                                            val document = task.result
                                            if (document != null && document.exists() && document.contains("name")) {
                                                navController.navigate("dashboard") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            } else {
                                                navController.navigate("profileSetup") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            }
                                        } else {
                                            // Agar Firestore fetch fail hua toh error message dikhao, freeze mat karo
                                            errorMessage = "Database Error: ${task.exception?.localizedMessage}"
                                        }
                                    }
                            } else {
                                isLoading = false
                            }
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            errorMessage = e.localizedMessage ?: "Invalid Email or Password!"
                        }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Login 🚪", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🔄 REDIRECT TO REGISTER: Naye users ko Register Screen par bhejne ke liye text button
            TextButton(
                onClick = { navController.navigate("register") }
            ) {
                Text(
                    text = "New User? Create Account Here 🚀",
                    color = Color(0xFF2196F3),
                    fontSize = 14.sp
                )
            }
        }
    }
}