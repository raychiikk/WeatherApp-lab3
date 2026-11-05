package com.example.weatherapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.weatherapp.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.data.City
import com.example.weatherapp.data.Prefs
import com.example.weatherapp.data.Weather
import com.example.weatherapp.data.WeatherApi
import com.example.weatherapp.data.WeatherCodes
import com.example.weatherapp.i18n.LocaleHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import kotlin.math.roundToInt

/**
 * Віджет головного екрана: показує поточну погоду для збереженого міста.
 * Оновлюється автоматично (раз на 30 хвилин), кнопкою ↻ та після оновлення в застосунку.
 */
class WeatherWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        Timber.d("Widget onUpdate, ids=%s", appWidgetIds.joinToString())
        refresh(context, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            Timber.d("Widget refresh requested")
            refresh(context, widgetIds(context))
        }
    }

    override fun onEnabled(context: Context) {
        Timber.i("First widget added to the home screen")
    }

    override fun onDisabled(context: Context) {
        Timber.i("Last widget removed from the home screen")
    }

    private fun refresh(context: Context, ids: IntArray) {
        if (ids.isEmpty()) return
        val app = context.applicationContext
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val localized = LocaleHelper.wrap(app)
                val city = Prefs(app).city
                val views = buildViews(app, localized, city)
                val manager = AppWidgetManager.getInstance(app)
                ids.forEach { manager.updateAppWidget(it, views) }
            } catch (e: Exception) {
                Timber.e(e, "Widget refresh failed")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun buildViews(app: Context, localized: Context, city: City?): RemoteViews {
        val views = RemoteViews(app.packageName, R.layout.widget_weather)
        views.setContentDescription(R.id.widget_refresh, localized.getString(R.string.widget_refresh_desc))
        views.setOnClickPendingIntent(R.id.widget_root, openAppIntent(app))
        views.setOnClickPendingIntent(R.id.widget_refresh, refreshIntent(app))

        if (city == null) {
            views.setTextViewText(R.id.widget_city, localized.getString(R.string.app_name))
            views.setTextViewText(R.id.widget_icon, "")
            views.setTextViewText(R.id.widget_temp, "")
            views.setTextViewText(R.id.widget_desc, localized.getString(R.string.widget_no_city))
            views.setTextViewText(R.id.widget_updated, "")
            return views
        }

        try {
            val weather = WeatherApi.loadWeather(city)
            bindWeather(views, localized, weather)
        } catch (e: IOException) {
            Timber.w(e, "Widget could not load weather for %s", city.name)
            views.setTextViewText(R.id.widget_city, city.name)
            views.setTextViewText(R.id.widget_icon, "")
            views.setTextViewText(R.id.widget_temp, "")
            views.setTextViewText(R.id.widget_desc, localized.getString(R.string.error_network))
            views.setTextViewText(R.id.widget_updated, "")
        }
        return views
    }

    private fun bindWeather(views: RemoteViews, localized: Context, weather: Weather) {
        views.setTextViewText(R.id.widget_city, weather.city.name)
        views.setTextViewText(R.id.widget_icon, WeatherCodes.emoji(weather.weatherCode, weather.isDay))
        views.setTextViewText(
            R.id.widget_temp,
            localized.getString(R.string.temp_format, weather.temperature.roundToInt())
        )
        views.setTextViewText(
            R.id.widget_desc,
            localized.getString(WeatherCodes.description(weather.weatherCode))
        )
        val time = android.text.format.DateFormat.getTimeFormat(localized)
            .format(java.util.Date(weather.fetchedAt))
        views.setTextViewText(R.id.widget_updated, localized.getString(R.string.last_updated, time))
    }

    private fun openAppIntent(context: Context): PendingIntent = PendingIntent.getActivity(
        context,
        REQUEST_OPEN,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun refreshIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_REFRESH,
        Intent(context, WeatherWidgetProvider::class.java).setAction(ACTION_REFRESH),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    companion object {
        const val ACTION_REFRESH = "com.example.weatherapp.ACTION_REFRESH_WIDGET"
        private const val REQUEST_OPEN = 1
        private const val REQUEST_REFRESH = 2

        private fun widgetIds(context: Context): IntArray =
            AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, WeatherWidgetProvider::class.java))

        /** Просить усі віджети оновитись (викликається після завантаження погоди в застосунку). */
        fun updateAll(context: Context) {
            val app = context.applicationContext
            if (widgetIds(app).isEmpty()) return
            app.sendBroadcast(
                Intent(app, WeatherWidgetProvider::class.java).setAction(ACTION_REFRESH)
            )
        }
    }
}
