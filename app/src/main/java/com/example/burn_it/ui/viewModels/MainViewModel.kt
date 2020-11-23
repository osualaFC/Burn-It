package com.example.burn_it.ui.viewModels


import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.burn_it.db.Run
import com.example.burn_it.repository.MainRepository
import kotlinx.coroutines.launch


class MainViewModel @ViewModelInject constructor(private val repository: MainRepository) : ViewModel() {

    fun insertRun(run: Run) = viewModelScope.launch{
        repository.insertRun(run)
    }
}