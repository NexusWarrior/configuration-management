

/**
 * Хранит конфигурацию приложения для анализа зависимостей пакетов.
 *
 * @property packageName Имя пакета, зависимости которого необходимо изучить.
 * @property repository Адрес репозитория или путь к тестовому файлу.
 * @property testMode Признак использования тестового режима.
 * @property outputFile Имя выходного PNG-файла с графом зависимостей.
 * @property filter Подстрока для исключения пакетов из результата.
 */
data class AppConfig(
    val packageName: String,
    val repository: String,
    val testMode: Boolean,
    val outputFile: String,
    val filter: String,
)
