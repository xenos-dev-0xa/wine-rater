package com.example.vine_rater.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Wine::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class WineDatabase : RoomDatabase() {
    abstract fun wineDao(): WineDao

    companion object {
        @Volatile
        private var Instance: WineDatabase? = null

        fun getDatabase(context: Context): WineDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, WineDatabase::class.java, "wine_database")
                    // .fallbackToDestructiveMigration() // Removed to prevent data loss on schema changes
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
