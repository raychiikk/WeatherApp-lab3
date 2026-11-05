package com.example.weatherapp.data

import androidx.annotation.StringRes
import com.example.weatherapp.R

/** Відповідність кодів погоди WMO (які повертає Open-Meteo) текстам та емодзі. */
object WeatherCodes {

    @StringRes
    fun description(code: Int): Int = when (code) {
        0 -> R.string.weather_clear
        1 -> R.string.weather_mainly_clear
        2 -> R.string.weather_partly_cloudy
        3 -> R.string.weather_overcast
        45, 48 -> R.string.weather_fog
        in 51..57 -> R.string.weather_drizzle
        in 61..65 -> R.string.weather_rain
        66, 67 -> R.string.weather_freezing_rain
        in 71..77 -> R.string.weather_snow
        in 80..82 -> R.string.weather_rain_showers
        85, 86 -> R.string.weather_snow_showers
        in 95..99 -> R.string.weather_thunderstorm
        else -> R.string.weather_unknown
    }

    fun emoji(code: Int, isDay: Boolean = true): String = when (code) {
        0 -> if (isDay) "☀️" else "🌙"
        1, 2 -> if (isDay) "⛅" else "☁️"
        3 -> "☁️"
        45, 48 -> "🌫️"
        in 51..57 -> "🌦️"
        in 61..65 -> "🌧️"
        66, 67 -> "🌨️"
        in 71..77 -> "❄️"
        in 80..82 -> "🌧️"
        85, 86 -> "🌨️"
        in 95..99 -> "⛈️"
        else -> "❔"
    }
}
