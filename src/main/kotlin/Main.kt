import kotlin.system.exitProcess

/**
 * Главная точка входа в приложение.
 *
 * Получает параметры командной строки, проверяет их
 * и выводит конфигурацию в формате «ключ=значение».
 *
 * @param args Аргументы командной строки.
 */
fun main(args: Array<String>) {
    try {
        // Получаем проверенную конфигурацию приложения.
        val config = CliParser.parse(args)

        // Выводим значения всех параметров.
        println("package=${config.packageName}")
        println("repository=${config.repository}")
        println("test-mode=${config.testMode}")
        println("output=${config.outputFile}")
        println("filter=${config.filter}")
    } catch (exception: IllegalArgumentException) {
        // Ошибки параметров отправляем в стандартный поток ошибок.
        System.err.println("Ошибка: ${exception.message}")

        // Ненулевой код сообщает об ошибочном завершении.
        exitProcess(1)
    }
}
