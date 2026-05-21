package com.example.codetrackai.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // Jab naya data aaye toh purane ko replace (update) kar do
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    // Live data observe karne ke liye Flow ka use karenge
    @Query("SELECT * FROM user_profile WHERE uid = :userId LIMIT 1")
    fun getUserProfile(userId: String): Flow<UserEntity?>
}