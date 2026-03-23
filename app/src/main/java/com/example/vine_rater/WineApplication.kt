package com.example.vine_rater

import android.app.Application
import com.example.vine_rater.data.WineDatabase
import com.example.vine_rater.data.WineRepository

class WineApplication : Application() {
    private val database by lazy { WineDatabase.getDatabase(this) }
    val repository by lazy { WineRepository(database.wineDao()) }
}
