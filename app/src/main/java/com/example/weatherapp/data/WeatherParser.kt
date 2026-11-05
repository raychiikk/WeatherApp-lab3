package com.example.weatherapp.data

import org.json.JSONObject

/** Розбір JSON-відповідей Open-Meteo. Окремо від мережі, щоб легко тестувати. */
object WeatherParser {

    /** Повертає перше знайдене місто або null, якщо збігів немає. */
    fun parseCity(json: String): City? {
        val results = JSONObject(json).optJSONArray("results") ?: return null
        if (results.length() == 0) return null
        val item = results.getJSONObject(0)
        return City(
            name = item.getString("name"),
            country = item.optString("country_code").takeIf { it.isNotEmpty() },
            latitude = item.getDouble("latitude"),
            longitude = item.getDouble("longitude")
        )
    }

    fun parseWeather(
        json: String,
        city: City,
        fetchedAt: Long = System.currentTimeMillis()
    ): Weather {
        val root = JSONObject(json)
        val current = root.getJSONObject("current")
        val daily = root.getJSONObject("daily")

        val dates = daily.getJSONArray("time")
        val codes = daily.getJSONArray("weather_code")
        val maxTemps = daily.getJSONArray("temperature_2m_max")
        val minTemps = daily.getJSONArray("temperature_2m_min")

        val days = List(dates.length()) { i ->
            DayForecast(
                date = dates.getString(i),
                weatherCode = codes.getInt(i),
                tempMax = maxTemps.getDouble(i),
                tempMin = minTemps.getDouble(i)
            )
        }

        return Weather(
            city = city,
            temperature = current.getDouble("temperature_2m"),
            feelsLike = current.getDouble("apparent_temperature"),
            humidity = current.getInt("relative_humidity_2m"),
            windSpeed = current.getDouble("wind_speed_10m"),
            weatherCode = current.getInt("weather_code"),
            isDay = current.getInt("is_day") == 1,
            daily = days,
            fetchedAt = fetchedAt
        )
    }
}
