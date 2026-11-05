package com.example.weatherapp.data

import android.content.Context
import androidx.core.content.edit

/** Локальні налаштування: останнє обране місто та мова інтерфейсу. */
class Prefs(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    var city: City?
        get() {
            val name = prefs.getString(KEY_CITY_NAME, null) ?: return null
            val lat = prefs.getString(KEY_CITY_LAT, null)?.toDoubleOrNull() ?: return null
            val lon = prefs.getString(KEY_CITY_LON, null)?.toDoubleOrNull() ?: return null
            return City(name, prefs.getString(KEY_CITY_COUNTRY, null), lat, lon)
        }
        set(value) = prefs.edit {
            if (value == null) {
                remove(KEY_CITY_NAME)
                remove(KEY_CITY_COUNTRY)
                remove(KEY_CITY_LAT)
                remove(KEY_CITY_LON)
            } else {
                putString(KEY_CITY_NAME, value.name)
                putString(KEY_CITY_COUNTRY, value.country)
                putString(KEY_CITY_LAT, value.latitude.toString())
                putString(KEY_CITY_LON, value.longitude.toString())
            }
        }

    /** Обрана мова (en, uk, de) або порожній рядок — мова системи. */
    var languageTag: String
        get() = prefs.getString(KEY_LANGUAGE, "") ?: ""
        set(value) = prefs.edit { putString(KEY_LANGUAGE, value) }

    private companion object {
        const val FILE_NAME = "weather_prefs"
        const val KEY_CITY_NAME = "city_name"
        const val KEY_CITY_COUNTRY = "city_country"
        const val KEY_CITY_LAT = "city_lat"
        const val KEY_CITY_LON = "city_lon"
        const val KEY_LANGUAGE = "language"
    }
}
