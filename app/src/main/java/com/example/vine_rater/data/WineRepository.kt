package com.example.vine_rater.data

import kotlinx.coroutines.flow.Flow

class WineRepository(private val wineDao: WineDao) {
    fun getAllWines(): Flow<List<Wine>> = wineDao.getAllWines()
    fun getWineById(id: Int): Flow<Wine?> = wineDao.getWineById(id)
    suspend fun insertWine(wine: Wine) = wineDao.insertWine(wine)
    suspend fun deleteWine(wine: Wine) = wineDao.deleteWine(wine)
}
