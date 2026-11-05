package com.example.weatherapp.logging

import android.util.Log
import timber.log.Timber

/**
 * Логер для release-збірки: пропускає DEBUG/INFO/VERBOSE,
 * щоб не засмічувати logcat і не виводити зайвих даних користувача.
 */
class ReleaseTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.WARN) return
        val text = if (t != null) "$message\n${Log.getStackTraceString(t)}" else message
        Log.println(priority, tag ?: DEFAULT_TAG, text)
    }

    private companion object {
        const val DEFAULT_TAG = "WeatherApp"
    }
}
