import kotlin.system.exitProcess

/**
 * Точка входа в приложение для анализа зависимостей NuGet.
 *
 * Получает конфигурацию и выводит прямые зависимости пакета.
 *
 * @param args Аргументы командной строки.
 */
fun main(args: Array<String>) {
    try {
        // Читаем параметры запуска приложения.
        val config = CliParser.parse(args)

        // Тестовый репозиторий графа реализуем на третьем этапе.
        require(!config.testMode) {
            "Тестовый режим будет реализован на этапе 3"
        }

        // Получаем прямые зависимости через NuGet V3 API.
        val packageInfo =
            NuGetClient().getDependencies(
                config.packageName,
                config.repository,
            )

        printDependencies(packageInfo)
    } catch (exception: IllegalArgumentException) {
        System.err.println("Ошибка: ${exception.message}")
        exitProcess(1)
    }
}

/**
 * Выводит пакет и его прямые зависимости по целевым платформам.
 */
private fun printDependencies(packageInfo: PackageInfo) {
    println("Пакет: ${packageInfo.name}")
    println("Версия: ${packageInfo.version}")
    println()
    println("Прямые зависимости:")

    if (packageInfo.groups.all { it.dependencies.isEmpty() }) {
        println("Прямые зависимости отсутствуют")
        return
    }

    for (group in packageInfo.groups) {
        println()
        println("Целевая платформа: ${group.targetFramework}")

        if (group.dependencies.isEmpty()) {
            println("  Зависимости отсутствуют")
        }

        for (dependency in group.dependencies) {
            println("  - ${dependency.name} [${dependency.versionRange}]")
        }
    }
}
