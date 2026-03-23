package com.example.vine_rater.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wines")
data class Wine(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val year: String, // Vintage year
    val photoUris: List<String>, // Multiple photos
    val type: String, // Red, White, Rosé, etc.
    val rating: String, // Very Good, Good, Average, Bad, Very Bad
    val boughtAt: String,
    val tasteNotes: String, // wood, grass, cork, etc.
    val smellNotes: String, // fruits, wood, etc.
    val customNotes: String,
    val dateAdded: Long = System.currentTimeMillis()
)
