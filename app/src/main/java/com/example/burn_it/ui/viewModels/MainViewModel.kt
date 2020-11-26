package com.example.burn_it.ui.viewModels


import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.burn_it.db.Run
import com.example.burn_it.db.Weather
import com.example.burn_it.repository.MainRepository
import com.example.burn_it.utils.SortType
import kotlinx.coroutines.launch
import retrofit2.Response
import timber.log.Timber
import java.io.IOException


class MainViewModel @ViewModelInject constructor(private val repository: MainRepository) : ViewModel() {

    private var _weatherData = MutableLiveData<Weather>()
    val weatherData: LiveData<Weather>
        get() = _weatherData

    fun insertRun(run: Run) = viewModelScope.launch{
        repository.insertRun(run)
    }

  fun getWeatherInfo(lat: Double, lon:Double){
      viewModelScope.launch {
          try{
              if(repository.todayWeather(lat, lon).isSuccessful){
                  _weatherData.value = repository.todayWeather(lat, lon).body()
              }
          }
          catch (e: IOException){
              Timber.i("response")
          }

      }
  }

    private val runsSortedByDate = repository.getAllRunsSortedByDate()
    private val runsSortedByDistance = repository.getAllRunsSortedByDistance()
    private val runsSortedByCaloriesBurned = repository.getAllRunsSortedByCaloriesBurned()
    private val runsSortedByTimeInMillis = repository.getAllRunsSortedByTimeInMillis()
    private val runsSortedByAvgSpeed = repository.getAllRunsSortedByAvgSpeed()

    val runs = MediatorLiveData<List<Run>>()

    var sortType = SortType.DATE

    init {
        runs.addSource(runsSortedByDate) { result ->
            if(sortType == SortType.DATE) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByAvgSpeed) { result ->
            if(sortType == SortType.AVG_SPEED) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByCaloriesBurned) { result ->
            if(sortType == SortType.CALORIES_BURNED) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByDistance) { result ->
            if(sortType == SortType.DISTANCE) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByTimeInMillis) { result ->
            if(sortType == SortType.RUNNING_TIME) {
                result?.let { runs.value = it }
            }
        }
    }

    fun sortRuns(sortType: SortType) = when(sortType) {
        SortType.DATE -> runsSortedByDate.value?.let { runs.value = it }
        SortType.RUNNING_TIME -> runsSortedByTimeInMillis.value?.let { runs.value = it }
        SortType.AVG_SPEED -> runsSortedByAvgSpeed.value?.let { runs.value = it }
        SortType.DISTANCE -> runsSortedByDistance.value?.let { runs.value = it }
        SortType.CALORIES_BURNED -> runsSortedByCaloriesBurned.value?.let { runs.value = it }
    }.also {
        this.sortType = sortType
    }
}

