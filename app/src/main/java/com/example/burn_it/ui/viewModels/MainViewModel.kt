package com.example.burn_it.ui.viewModels


import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.burn_it.repository.MainRepository


class MainViewModel @ViewModelInject constructor(val repository: MainRepository) : ViewModel() {
}