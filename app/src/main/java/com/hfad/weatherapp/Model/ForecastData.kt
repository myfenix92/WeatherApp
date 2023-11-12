package com.hfad.weatherapp.Model

data class ForecastData(
    val list: List<Forecast>,
    val message: Int
) {
    data class Forecast(
        val dt: Int,
        val dt_txt: String,
        val main: Main,
        val visibility: Int,
        val weather: List<Weather>,
    ) {
        data class Main(
            val temp_max: Double,
            val temp_min: Double
        )

        data class Weather(
            val description: String,
            val icon: String,
            val id: Int
        )
    }
}