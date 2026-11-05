package com.example.weatherapp.data

/** Місто, знайдене через geocoding API. [country] — двобуквений код країни. */
data class City(
    val name: String,
    val country: String?,
    val latitude: Double,
    val longitude: Double
)

/** Прогноз на один день. [date] у форматі yyyy-MM-dd. */
data class DayForecast(
    val date: String,
    val weatherCode: Int,
    val tempMax: Double,
    val tempMin: Double
)

/** Поточна погода та прогноз на кілька днів. Температура в °C, вітер у км/год. */
data class Weather(
    val city: City,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val weatherCode: Int,
    val isDay: Boolean,
    val daily: List<DayForecast>,
    val fetchedAt: Long = System.currentTimeMillis()
)
