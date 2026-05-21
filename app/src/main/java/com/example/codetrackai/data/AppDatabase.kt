package com.example.codetrackai.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// 🌟 NEW: Version ko 1 se badal kar 2 kar diya taaki naye schema fields apply ho sakein
@Database(entities = [UserEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "codetrackai_db"
                )
                    .fallbackToDestructiveMigration() // Naye version par ate hi automatic schema reset kar dega bina crash ke
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}