package com.example.codetrackai

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun saveUserData(
        name: String,
        problems: Int,
        leetcode: Int,
        codeforces: Int,
        tasks: List<Task>
    ) {
        val userId = auth.currentUser?.uid ?: return

        // 🛠️ FIX: Keys ko totalLeetCodeSolved aur totalCodeforcesSolved kar diya taaki ViewModel se match ho
        val data = hashMapOf(
            "name" to name,
            "problemsSolved" to problems,
            "totalLeetCodeSolved" to leetcode,
            "totalCodeforcesSolved" to codeforces,
            "tasks" to tasks
        )

        db.collection("users")
            .document(userId)
            .set(data)
    }

    fun getUserData(onResult: (Map<String, Any>?) -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    onResult(document.data)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }
}