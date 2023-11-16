package com.hfad.weatherapp.Model

data class ForecastData(
    val list: List<Forecast>,
) {
    data class Forecast(
        val dt: Int?,
        val dt_txt: String?,
        val main: Main?,
        val weather: List<Weather>,
    ) {
        data class Main(
            val temp_min: Double?,
            val temp_max: Double?
        )

        data class Weather(
            val description: String?,
            val icon: String?,
            val id: Int?
        )
    }
}