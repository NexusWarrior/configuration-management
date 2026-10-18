import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.Locale

/**
 * Получает информацию о пакетах через официальный NuGet V3 API.
 *
 * Для HTTP используется стандартный клиент Java.
 */
class NuGetClient {
    private val client =
        HttpClient
            .newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()

    /**
     * Загружает прямые зависимости последней стабильной версии пакета.
     *
     * @param packageName Имя анализируемого пакета.
     * @param repository URL служебного индекса NuGet V3.
     */
    fun getDependencies(
        packageName: String,
        repository: String,
    ): PackageInfo {
        require(Regex("[A-Za-z0-9_.-]+").matches(packageName)) {
            "Некорректное имя NuGet-пакета"
        }

        val baseUrl = getPackageBaseUrl(repository)
        val normalizedName = packageName.lowercase(Locale.ROOT)

        val versionsUrl = "$baseUrl/$normalizedName/index.json"
        val versionsJson = get(versionsUrl)
        val version = selectLatestStableVersion(versionsJson)

        val nuspecUrl = "$baseUrl/$normalizedName/$version/$normalizedName.nuspec"
        val nuspecXml = get(nuspecUrl)
        val groups = NuspecParser.parse(nuspecXml)

        return PackageInfo(packageName, version, groups)
    }

    /**
     * Находит URL ресурса PackageBaseAddress в Service Index.
     */
    private fun getPackageBaseUrl(repository: String): String {
        val root = JsonParser(get(repository)).parse().asObject()
        val resources = root["resources"].asArray()

        for (resource in resources) {
            val item = resource.asObject()
            val types =
                when (val type = item["@type"]) {
                    is String -> listOf(type)
                    is List<*> -> type.filterIsInstance<String>()
                    else -> emptyList()
                }

            if ("PackageBaseAddress/3.0.0" in types) {
                val url =
                    item["@id"] as? String
                        ?: throw IllegalArgumentException(
                            "Не указан адрес PackageBaseAddress",
                        )

                return url.trimEnd('/')
            }
        }

        throw IllegalArgumentException(
            "Репозиторий не поддерживает PackageBaseAddress/3.0.0",
        )
    }

    /**
     * Находит последнюю стабильную версию пакета.
     */
    private fun selectLatestStableVersion(json: String): String {
        val root = JsonParser(json).parse().asObject()

        val versions =
            root["versions"]
                .asArray()
                .map { value ->
                    value as? String ?: throw IllegalArgumentException(
                        "Некорректная версия в ответе NuGet",
                    )
                }.filter { '-' !in it }

        require(versions.isNotEmpty()) {
            "У пакета отсутствуют стабильные версии"
        }

        val versionFormat = Regex("[0-9]+(\\.[0-9]+){1,3}")

        require(versions.all { versionFormat.matches(it) }) {
            "Некорректный формат стабильной версии NuGet"
        }

        return versions.maxWith(
            Comparator { left, right -> compareVersions(left, right) },
        )
    }

    /**
     * Сравнивает числовые компоненты стабильных версий NuGet.
     */
    private fun compareVersions(
        left: String,
        right: String,
    ): Int {
        val leftParts = left.split('.').map { it.toLong() }
        val rightParts = right.split('.').map { it.toLong() }
        val count = maxOf(leftParts.size, rightParts.size)

        for (index in 0 until count) {
            val first = leftParts.getOrElse(index) { 0L }
            val second = rightParts.getOrElse(index) { 0L }
            val comparison = first.compareTo(second)

            if (comparison != 0) return comparison
        }

        return 0
    }

    /**
     * Выполняет HTTP GET-запрос и возвращает тело ответа.
     */
    private fun get(url: String): String {
        val request =
            HttpRequest
                .newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build()

        val response =
            try {
                client.send(request, HttpResponse.BodyHandlers.ofString())
            } catch (exception: IOException) {
                throw IllegalArgumentException(
                    "Не удалось получить данные NuGet: ${exception.message}",
                    exception,
                )
            } catch (exception: InterruptedException) {
                Thread.currentThread().interrupt()
                throw IllegalArgumentException(
                    "Запрос NuGet был прерван",
                    exception,
                )
            }

        require(response.statusCode() == 200) {
            "Ошибка NuGet HTTP ${response.statusCode()}: $url"
        }

        return response.body()
    }

    /**
     * Преобразует JSON-значение в объект.
     */
    private fun Any?.asObject(): Map<*, *> =
        this as? Map<*, *>
            ?: throw IllegalArgumentException("Ожидался JSON-объект")

    /**
     * Преобразует JSON-значение в массив.
     */
    private fun Any?.asArray(): List<*> =
        this as? List<*>
            ?: throw IllegalArgumentException("Ожидался JSON-массив")
}
