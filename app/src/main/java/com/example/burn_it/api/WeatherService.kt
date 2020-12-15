package com.example.burn_it.api

import com.example.burn_it.BuildConfig.API_KEY
import com.example.burn_it.db.Weather
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {

    @GET("weather?")
    suspend fun getTodayWeather(
        @Query("lat")
        latitude: Double = 0.0,

        @Query("lon")
        longitude: Double = 0.0,

        @Query("appid")
        apiKey: String = API_KEY
    ): Response<Weather>
}