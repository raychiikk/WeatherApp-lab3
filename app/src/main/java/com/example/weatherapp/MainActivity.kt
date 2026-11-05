package com.example.weatherapp

import android.os.Bundle
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.data.City
import com.example.weatherapp.data.Prefs
import com.example.weatherapp.data.Weather
import com.example.weatherapp.data.WeatherApi
import com.example.weatherapp.data.WeatherCodes
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.databinding.ItemForecastBinding
import com.example.weatherapp.i18n.LocaleHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        prefs = Prefs(this)
        LocaleHelper.syncStoredTag(this)
        Timber.i("MainActivity created, language=%s", currentLanguage())

        binding.searchButton.setOnClickListener { submitSearch() }
        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                submitSearch()
                true
            } else {
                false
            }
        }

        val savedCity = prefs.city
        if (savedCity != null) {
            loadWeather(savedCity)
        } else {
            showMessage(getString(R.string.hint_enter_city))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_refresh -> {
            prefs.city?.let { loadWeather(it) }
            true
        }
        R.id.action_language -> {
            showLanguageDialog()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun submitSearch() {
        val query = binding.searchInput.text?.toString().orEmpty().trim()
        if (query.isEmpty()) {
            showMessage(getString(R.string.hint_enter_city))
            return
        }
        hideKeyboard()
        Timber.i("Search submitted: '%s'", query)
        launchLoading {
            val city = WeatherApi.findCity(query, currentLanguage())
            if (city == null) {
                Timber.w("City not found: '%s'", query)
                showMessage(getString(R.string.error_city_not_found, query))
            } else {
                prefs.city = city
                showWeather(WeatherApi.loadWeather(city))
            }
        }
    }

    private fun loadWeather(city: City) {
        launchLoading { showWeather(WeatherApi.loadWeather(city)) }
    }

    /** Запускає мережеву операцію з індикатором завантаження та єдиною обробкою помилок. */
    private fun launchLoading(block: suspend () -> Unit) {
        lifecycleScope.launch {
            setLoading(true)
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                Timber.e(e, "Network error while loading weather")
                showMessage(getString(R.string.error_network))
            } catch (e: Exception) {
                Timber.e(e, "Unexpected error while loading weather")
                showMessage(getString(R.string.error_generic))
            } finally {
                setLoading(false)
            }
        }
    }

    private fun showWeather(weather: Weather) {
        binding.messageText.visibility = View.GONE
        binding.weatherCard.visibility = View.VISIBLE
        binding.forecastTitle.visibility = View.VISIBLE

        val cityTitle = listOfNotNull(weather.city.name, weather.city.country).joinToString(", ")
        binding.cityText.text = cityTitle
        binding.iconText.text = WeatherCodes.emoji(weather.weatherCode, weather.isDay)
        binding.tempText.text = getString(R.string.temp_format, weather.temperature.roundToInt())
        binding.descText.setText(WeatherCodes.description(weather.weatherCode))
        binding.detailsText.text = getString(
            R.string.details_format,
            weather.feelsLike.roundToInt(),
            weather.humidity,
            weather.windSpeed.roundToInt()
        )
        binding.updatedText.text = getString(
            R.string.last_updated,
            DateFormat.getTimeFormat(this).format(Date(weather.fetchedAt))
        )

        val locale = resources.configuration.locales[0]
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val formatter = SimpleDateFormat("EEE, d MMM", locale)

        binding.forecastContainer.removeAllViews()
        weather.daily.forEach { day ->
            val item = ItemForecastBinding.inflate(layoutInflater, binding.forecastContainer, false)
            item.dayText.text = parser.parse(day.date)?.let(formatter::format) ?: day.date
            item.iconText.text = WeatherCodes.emoji(day.weatherCode)
            item.rangeText.text = getString(
                R.string.forecast_range,
                day.tempMax.roundToInt(),
                day.tempMin.roundToInt()
            )
            binding.forecastContainer.addView(item.root)
        }
        Timber.d("Weather rendered for %s", cityTitle)
    }

    private fun showMessage(text: String) {
        binding.weatherCard.visibility = View.GONE
        binding.forecastTitle.visibility = View.GONE
        binding.forecastContainer.removeAllViews()
        binding.messageText.text = text
        binding.messageText.visibility = View.VISIBLE
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.searchButton.isEnabled = !loading
    }

    private fun showLanguageDialog() {
        val tags = LocaleHelper.LANGUAGE_TAGS
        val names = arrayOf(
            getString(R.string.language_system),
            getString(R.string.language_name_en),
            getString(R.string.language_name_uk),
            getString(R.string.language_name_de)
        )
        val checked = tags.indexOf(LocaleHelper.currentTag()).coerceAtLeast(0)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.language_dialog_title)
            .setSingleChoiceItems(names, checked) { dialog, which ->
                dialog.dismiss()
                LocaleHelper.setLanguage(this, tags[which])
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun currentLanguage(): String = resources.configuration.locales[0].language

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchInput.windowToken, 0)
    }
}
