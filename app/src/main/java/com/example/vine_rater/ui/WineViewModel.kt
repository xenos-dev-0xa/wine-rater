package com.example.vine_rater.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.vine_rater.WineApplication
import com.example.vine_rater.data.Wine
import com.example.vine_rater.data.WineRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WineViewModel(val repository: WineRepository) : ViewModel() {

    val allWines: StateFlow<List<Wine>> = repository.getAllWines()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertWine(wine: Wine) {
        viewModelScope.launch {
            repository.insertWine(wine)
        }
    }

    fun deleteWine(wine: Wine) {
        viewModelScope.launch {
            repository.deleteWine(wine)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WineApplication)
                WineViewModel(application.repository)
            }
        }
    }
}
