import java.io.File
import java.net.URI

/**
 * Обрабатывает и проверяет аргументы командной строки.
 *
 * Преобразует переданные параметры в объект [AppConfig].
 */
object CliParser {
    // Набор параметров, которые поддерживает приложение.
    private val supportedOptions =
        setOf(
            "--package",
            "--repository",
            "--test-mode",
            "--output",
            "--filter",
        )

    /**
     * Разбирает аргументы командной строки.
     *
     * @param args Массив аргументов, полученных при запуске программы.
     * @return Конфигурация с проверенными значениями параметров.
     * @throws IllegalArgumentException Если параметры заданы неверно.
     */
    fun parse(args: Array<String>): AppConfig {
        val options = readOptions(args)

        val packageName = required(options, "--package")
        val repository = required(options, "--repository")
        val testMode = parseTestMode(required(options, "--test-mode"))
        val outputFile = required(options, "--output")
        val filter = options["--filter"] ?: ""

        validateRepository(repository, testMode)
        validateOutput(outputFile)

        return AppConfig(
            packageName,
            repository,
            testMode,
            outputFile,
            filter,
        )
    }

    /**
     * Преобразует последовательность аргументов в словарь.
     *
     * Каждый параметр должен сопровождаться значением.
     * Повторное указание одного параметра запрещено.
     */
    private fun readOptions(args: Array<String>): Map<String, String> {
        val options = mutableMapOf<String, String>()
        var index = 0

        while (index < args.size) {
            val key = args[index]

            require(key in supportedOptions) {
                "Неизвестный параметр: $key"
            }
            require(key !in options) {
                "Параметр указан повторно: $key"
            }
            require(index + 1 < args.size) {
                "Отсутствует значение параметра: $key"
            }

            val value = args[index + 1]
            require(value !in supportedOptions && !value.startsWith("--")) {
                "Отсутствует значение параметра: $key"
            }

            options[key] = value
            index += 2
        }

        return options
    }

    /**
     * Возвращает значение обязательного параметра.
     *
     * @throws IllegalArgumentException Если значение отсутствует или пустое.
     */
    private fun required(
        options: Map<String, String>,
        key: String,
    ): String {
        val value = options[key]

        require(!value.isNullOrBlank()) {
            "Не указан обязательный параметр: $key"
        }

        return value
    }

    /**
     * Преобразует строковое значение тестового режима в Boolean.
     */
    private fun parseTestMode(value: String): Boolean =
        when (value) {
            "true" -> true

            "false" -> false

            else -> throw IllegalArgumentException(
                "Параметр --test-mode должен иметь значение true или false",
            )
        }

    /**
     * Проверяет репозиторий в зависимости от режима работы.
     *
     * В тестовом режиме требуется существующий файл.
     * В обычном режиме требуется корректный HTTP или HTTPS URL.
     */
    private fun validateRepository(
        repository: String,
        testMode: Boolean,
    ) {
        if (testMode) {
            require(File(repository).isFile) {
                "Тестовый файл не найден: $repository"
            }
            return
        }

        val uri =
            try {
                URI(repository)
            } catch (exception: IllegalArgumentException) {
                throw IllegalArgumentException(
                    "Некорректный адрес репозитория",
                    exception,
                )
            }

        require(uri.scheme in setOf("http", "https") && !uri.host.isNullOrBlank()) {
            "Репозиторий должен иметь корректный HTTP или HTTPS адрес"
        }
    }

    /**
     * Проверяет имя выходного файла.
     *
     * Для визуализации графа поддерживается формат PNG.
     */
    private fun validateOutput(outputFile: String) {
        val fileName = File(outputFile).name

        require(fileName.endsWith(".png", ignoreCase = true)) {
            "Выходной файл должен иметь расширение .png"
        }
        require(fileName.substringBeforeLast('.').isNotBlank()) {
            "Имя выходного файла не может быть пустым"
        }
    }
}
