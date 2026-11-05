package com.example.weatherapp.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherParserTest {

    private val forecastJson = """
        {
          "current": {
            "temperature_2m": 7.4,
            "apparent_temperature": 4.9,
            "relative_humidity_2m": 81,
            "wind_speed_10m": 12.6,
            "weather_code": 61,
            "is_day": 0
          },
          "daily": {
            "time": ["2026-11-05", "2026-11-06"],
            "weather_code": [61, 3],
            "temperature_2m_max": [9.1, 8.0],
            "temperature_2m_min": [3.2, 2.5]
          }
        }
    """.trimIndent()

    @Test
    fun parseCity_returnsFirstResult() {
        val json = """
            {"results":[{"name":"Kremenchuk","country_code":"UA","latitude":49.07,"longitude":33.42}]}
        """.trimIndent()

        val city = WeatherParser.parseCity(json)

        assertNotNull(city)
        assertEquals("Kremenchuk", city!!.name)
        assertEquals("UA", city.country)
        assertEquals(49.07, city.latitude, 0.0001)
        assertEquals(33.42, city.longitude, 0.0001)
    }

    @Test
    fun parseCity_returnsNullWhenNothingFound() {
        assertNull(WeatherParser.parseCity("""{"generationtime_ms":0.5}"""))
        assertNull(WeatherParser.parseCity("""{"results":[]}"""))
    }

    @Test
    fun parseWeather_readsCurrentAndDailyData() {
        val city = City("Test", "UA", 50.0, 30.0)

        val weather = WeatherParser.parseWeather(forecastJson, city, fetchedAt = 123L)

        assertEquals(7.4, weather.temperature, 0.0001)
        assertEquals(4.9, weather.feelsLike, 0.0001)
        assertEquals(81, weather.humidity)
        assertEquals(12.6, weather.windSpeed, 0.0001)
        assertEquals(61, weather.weatherCode)
        assertFalse(weather.isDay)
        assertEquals(123L, weather.fetchedAt)
        assertEquals(2, weather.daily.size)
        assertEquals("2026-11-06", weather.daily[1].date)
        assertEquals(3, weather.daily[1].weatherCode)
        assertEquals(2.5, weather.daily[1].tempMin, 0.0001)
        assertTrue(weather.daily[0].tempMax > weather.daily[0].tempMin)
    }
}
