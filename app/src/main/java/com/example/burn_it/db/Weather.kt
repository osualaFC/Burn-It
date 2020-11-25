package com.example.burn_it.db


import com.google.gson.annotations.SerializedName

data class Weather(
    @SerializedName("id")
    val id: Int,
    @SerializedName("main")
    val main: Main,
    @SerializedName("weather")
    val weather: List<WeatherX>,
    @SerializedName("name")
    val name: String
)