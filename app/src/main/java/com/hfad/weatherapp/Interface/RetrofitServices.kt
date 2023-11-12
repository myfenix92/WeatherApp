package com.hfad.weatherapp.Interface

import com.hfad.weatherapp.Model.ForecastData
import com.hfad.weatherapp.Model.WeatherData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitServices {
    @GET("weather?units=metric&appid=cfab30bb66d89e8952405563767c1480")
    suspend fun getWeather(
        @Query("q") town: String,
        @Query("lang") lang: String) : Response<WeatherData>
    @GET("forecast?units=metric&appid=cfab30bb66d89e8952405563767c1480")
    suspend fun getForecast(
        @Query("q") town: String,
        @Query("lang") lang: String) : Response<ForecastData>
}