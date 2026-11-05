# WeatherApp — Лабораторна робота 3, варіант 10

Android-застосунок «Погода» на Kotlin: поточна погода та прогноз на 5 днів для обраного міста
(дані з [Open-Meteo](https://open-meteo.com), ключ API не потрібен) і віджет для головного екрана.

## Відповідність вимогам

| Вимога | Реалізація |
|---|---|
| Kotlin/Java | Kotlin, XML + ViewBinding |
| Віддалений репозиторій, логічні коміти, `.gitignore` | Історія в `git log`, `.gitignore` для Android/Gradle/IDE (див. «Публікація в репозиторій») |
| Логування | [Timber](https://github.com/JakeWharton/Timber): `DebugTree` у debug, `ReleaseTree` (лише WARN/ERROR) у release; ініціалізація в `WeatherApp` |
| i18n | Англійська (за замовчуванням), українська, німецька: `values/`, `values-uk/`, `values-de/`; per-app language (`AppCompatDelegate.setApplicationLocales`, `locales_config.xml`); перемикач у меню застосунку |
| Варіант 10 | Використання API (Open-Meteo: геокодинг + прогноз) та віджет `WeatherWidgetProvider` |

## Можливості

- Пошук міста за назвою, збереження обраного міста.
- Поточна температура, відчувається як, вологість, вітер, опис погоди (коди WMO → локалізований текст + емодзі).
- Прогноз на 5 днів.
- Віджет 3×2: місто, температура, опис, час оновлення; оновлення раз на 30 хв, кнопкою ↻ та після оновлення в застосунку; натискання відкриває застосунок.
- Обробка помилок: немає мережі, місто не знайдено, непередбачена помилка.

## Структура

```
app/src/main/java/com/example/weatherapp/
├── WeatherApp.kt          # Application, налаштування Timber
├── MainActivity.kt        # головний екран
├── data/                  # Models, WeatherApi, WeatherParser, WeatherCodes, Prefs
├── i18n/LocaleHelper.kt   # перемикання мови
├── logging/ReleaseTree.kt # логер для release
└── widget/WeatherWidgetProvider.kt
app/src/test/              # юніт-тести парсера JSON
```

## Запуск

1. Відкрити папку проєкту в Android Studio (Ladybug або новіша), дочекатися Gradle Sync.
2. Запустити на емуляторі чи пристрої з Android 7.0+ (minSdk 24).
3. Віджет: довге натискання на головному екрані → «Віджети» → «Weather».
4. Тести: `./gradlew test`.
5. Логи: у Logcat фільтр за тегами `MainActivity`, `WeatherApi`, `WeatherWidgetProvider`.

## Публікація в репозиторій

```bash
git remote add origin https://github.com/<ваш-логін>/<назва-репозиторію>.git
git push -u origin main
```

## Примітки

- Мова віджета береться з налаштування застосунку, яке зберігається в `Prefs`.
- Назва міста зберігається тією мовою, якою було виконано пошук.
