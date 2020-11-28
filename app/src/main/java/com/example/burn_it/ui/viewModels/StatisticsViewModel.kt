package com.example.burn_it.ui.viewModels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.burn_it.repository.MainRepository
import kotlinx.coroutines.launch

class StatisticsViewModel @ViewModelInject constructor(private val repository: MainRepository) :ViewModel(){

    val totalTimeRun = repository.getTotalTimeInMillis()
    val totalDistance = repository.getTotalDistance()
    val totalCaloriesBurned = repository.getTotalCaloriesBurned()
    val totalAvgSpeed = repository.getTotalAvgSpeed()

    val runsSortedByDate = repository.getAllRunsSortedByDate()

    fun clearData() = viewModelScope.launch {
        repository.deleteAll()
    }
}