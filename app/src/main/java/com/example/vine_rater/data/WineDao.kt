package com.example.vine_rater.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WineDao {
    @Query("SELECT * FROM wines ORDER BY dateAdded DESC")
    fun getAllWines(): Flow<List<Wine>>

    @Query("SELECT * FROM wines WHERE id = :id")
    fun getWineById(id: Int): Flow<Wine?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWine(wine: Wine)

    @Delete
    suspend fun deleteWine(wine: Wine)
}
