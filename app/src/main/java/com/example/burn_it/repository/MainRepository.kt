package com.example.burn_it.repository

import com.example.burn_it.api.WeatherService
import com.example.burn_it.db.Run
import com.example.burn_it.db.RunDAO
import com.example.burn_it.db.Weather
import retrofit2.Response
import javax.inject.Inject

class MainRepository @Inject constructor(private val runDao: RunDAO, private val api: WeatherService) {

    suspend fun insertRun(run: Run) = runDao.insertRun(run)

    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)

    fun getAllRunsSortedByDate() = runDao.getAllRunsSortedByDate()

    fun getAllRunsSortedByDistance() = runDao.getAllRunsSortedByDistance()

    fun getAllRunsSortedByTimeInMillis() = runDao.getAllRunsSortedByTimeInMillis()

    fun getAllRunsSortedByAvgSpeed() = runDao.getAllRunsSortedByAvgSpeed()

    fun getAllRunsSortedByCaloriesBurned() = runDao.getAllRunsSortedByCaloriesBurned()

    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()

    fun getTotalDistance() = runDao.getTotalDistance()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()

    fun getTotalTimeInMillis() = runDao.getTotalTimeInMillis()

    suspend fun todayWeather(lat: Double, lon: Double): Response<Weather> = api.getTodayWeather(lat, lon)
}