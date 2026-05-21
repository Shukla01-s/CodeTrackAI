package com.example.codetrackai.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val problemsSolved: Int,
    val streak: Int,
    val totalLeetCodeSolved: Int,
    val totalCodeforcesSolved: Int,
    // 🌟 NEW: User ki handles ko cache karne ke liye variables add kiye hain (Purane code ko bina chhede)
    val leetcodeHandle: String = "",
    val codeforcesHandle: String = ""
)