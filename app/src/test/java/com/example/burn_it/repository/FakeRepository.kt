package com.example.burn_it.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.burn_it.db.Main
import com.example.burn_it.db.Run
import com.example.burn_it.db.Weather
import retrofit2.Response

class FakeRepository  {

    /**fake room db*/
    private val runs = mutableListOf<Run>()

    /**fake live data*/
    private val runsSortedByDate = MutableLiveData<List<Run>>(runs)
    private val totalDistance = MutableLiveData<Int>()

    /**simulate network response**/
    private var returnNetworkError = false

    private fun setReturnNetworkError(value : Boolean){
        returnNetworkError = value
    }

    private fun getTotalDistance():Int{
        return runs.sumBy { it.distanceInMeters }
    }

    /**update fake live data**/
   private fun updateLiveData(){
        runsSortedByDate.postValue(runs)
        totalDistance.postValue(getTotalDistance())
    }

    suspend fun insertRun(run: Run){
        runs.add(run)
        updateLiveData()
    }

    suspend fun deleteRun(run: Run){
        runs.remove(run)
        updateLiveData()
    }

    fun allRunsSortedByDate(): LiveData<List<Run>>{
        return runsSortedByDate
    }

    fun distanceSum():LiveData<Int>{
        return totalDistance
    }

    suspend fun todayWeather(lat: Double, long:Double): Response<Weather>{
      return  if(returnNetworkError){
            Response.error<Weather>(404, null)
        }
        else{
            Response.success(Weather(0, Main(0,0,0.0,0.0,0.0), listOf(), "weather"))
        }
    }


}