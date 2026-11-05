package com.example.weatherapp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Клієнт Open-Meteo (https://open-meteo.com): геокодинг міста та прогноз погоди.
 * API безкоштовне й не потребує ключа.
 */
object WeatherApi {

    private const val GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search"
    private const val FORECAST_URL = "https://api.open-meteo.com/v1/forecast"
    private const val TIMEOUT_MS = 10_000
    private const val FORECAST_DAYS = 5

    /** Шукає місто за назвою. [language] — код мови для локалізації назви (en, uk, de). */
    suspend fun findCity(query: String, language: String): City? = withContext(Dispatchers.IO) {
        Timber.d("Geocoding request for '%s' (lang=%s)", query, language)
        val encoded = URLEncoder.encode(query, "UTF-8")
        val url = "$GEOCODING_URL?name=$encoded&count=1&language=$language&format=json"
        WeatherParser.parseCity(httpGet(url)).also {
            Timber.d("Geocoding result: %s", it)
        }
    }

    suspend fun loadWeather(city: City): Weather = withContext(Dispatchers.IO) {
        Timber.d("Forecast request for %s (%.3f, %.3f)", city.name, city.latitude, city.longitude)
        val url = "$FORECAST_URL?latitude=${city.latitude}&longitude=${city.longitude}" +
            "&current=temperature_2m,apparent_temperature,relative_humidity_2m," +
            "wind_speed_10m,weather_code,is_day" +
            "&daily=weather_code,temperature_2m_max,temperature_2m_min" +
            "&timezone=auto&forecast_days=$FORECAST_DAYS"
        WeatherParser.parseWeather(httpGet(url), city).also {
            Timber.i("Weather loaded for %s: %.1f°C, code=%d", city.name, it.temperature, it.weatherCode)
        }
    }

    private fun httpGet(urlString: String): String {
        val connection = URL(urlString).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            val status = connection.responseCode
            if (status != HttpURLConnection.HTTP_OK) {
                throw IOException("Unexpected HTTP status $status")
            }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
