package com.hfad.weatherapp.Model

import com.hfad.weatherapp.IWeather

data class WeatherData(
    val clouds: Clouds,
    val dt: Long,
    val cod: Int,
    val main: Main,
    val sys: Sys,
    val visibility: Int,
    var rain: Rain,
    val weather: List<Weather>,
    val wind: Wind,
    val timezone: Int
) {
    data class Clouds(
        val all: Int
    )

    data class Main(
        val feels_like: Double,
        val humidity: Int,
        val pressure: Int,
        val temp: Double,
        val temp_max: Double,
        val temp_min: Double
    )

    data class Sys(
        val sunrise: Long,
        val sunset: Long
    )
    data class Rain(
        val `1h`: Double?
    )
    data class Weather(
        val description: String,
        val icon: String,
        val id: Int
    )

    data class Wind(
        val deg: Int,
        val speed: Double
    )
}