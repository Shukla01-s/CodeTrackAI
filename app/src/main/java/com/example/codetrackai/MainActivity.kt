package com.example.codetrackai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.codetrackai.ui.theme.CodeTrackAITheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        setContent {
            CodeTrackAITheme {
                val navController = rememberNavController()

                // Agar user already logged in hai toh direct dashboard par bhejenge
                val startDestination = if (auth.currentUser != null) "dashboard" else "login"

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("login") {
                        LoginScreen(navController = navController, auth = auth, db = db)
                    }
                    composable("register") {
                        RegisterScreen(navController = navController, auth = auth)
                    }
                    composable("profileSetup") {
                        ProfileSetupScreen(navController = navController, auth = auth, db = db)
                    }

                    composable("dashboard") {
                        val application = this@MainActivity.application
                        val viewModel: DashboardViewModel = ViewModelProvider(
                            this@MainActivity,
                            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                        )[DashboardViewModel::class.java]

                        DashboardScreen(navController = navController, viewModel = viewModel)
                    }
                    composable("tasks") {
                        val application = this@MainActivity.application
                        val viewModel: DashboardViewModel = ViewModelProvider(
                            this@MainActivity,
                            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                        )[DashboardViewModel::class.java]

                        TaskScreen(
                            navController = navController,
                            viewModel = viewModel
                        )
                    }

                    // 🌟 NEW: Premium Custom Profile Rating Screen Route Add Kiya Hai
                    composable("profile") {
                        val application = this@MainActivity.application
                        val viewModel: DashboardViewModel = ViewModelProvider(
                            this@MainActivity,
                            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                        )[DashboardViewModel::class.java]

                        ProfileScreen(
                            navController = navController,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}