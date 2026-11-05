package com.example.weatherapp

import android.app.Application
import com.example.weatherapp.logging.ReleaseTree
import timber.log.Timber

/**
 * Точка входу застосунку. Тут один раз налаштовується логер Timber:
 * у debug-збірці — повний вивід, у release — лише попередження та помилки.
 */
class WeatherApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
        Timber.i("Application started, version %s", BuildConfig.VERSION_NAME)
    }
}
