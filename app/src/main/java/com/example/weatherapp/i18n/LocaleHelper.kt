package com.example.weatherapp.i18n

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.weatherapp.data.Prefs
import timber.log.Timber
import java.util.Locale

/**
 * Перемикання мови застосунку. Використовує per-app language з AppCompat
 * (на Android 13+ мову також можна змінити в системних налаштуваннях застосунку).
 */
object LocaleHelper {

    /** Порожній тег означає «мова системи». */
    val LANGUAGE_TAGS = listOf("", "en", "uk", "de")

    /** Поточна мова застосунку: en, uk, de або "" (системна). */
    fun currentTag(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return ""
        return locales.toLanguageTags().substringBefore(',').substringBefore('-')
    }

    fun setLanguage(context: Context, tag: String) {
        Timber.i("Language changed to '%s'", tag.ifEmpty { "system" })
        Prefs(context).languageTag = tag
        AppCompatDelegate.setApplicationLocales(
            if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList()
            else LocaleListCompat.forLanguageTags(tag)
        )
    }

    /** Синхронізує збережений тег із фактичною мовою (її могли змінити в налаштуваннях системи). */
    fun syncStoredTag(context: Context) {
        Prefs(context).languageTag = currentTag()
    }

    /**
     * Контекст із мовою, обраною в застосунку. Потрібен для віджета,
     * який працює поза Activity і не отримує мову AppCompat автоматично.
     */
    fun wrap(context: Context): Context {
        val tag = Prefs(context).languageTag
        if (tag.isEmpty()) return context
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale.forLanguageTag(tag))
        return context.createConfigurationContext(config)
    }
}
