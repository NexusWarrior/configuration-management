import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.file.Files

/**
 * Проверяет обработку аргументов командной строки.
 *
 * Тесты охватывают правильные параметры и типичные ошибки.
 */
class CliParserTest {
    /**
     * Создаёт набор корректных аргументов для тестирования.
     */
    private fun validArgs() =
        arrayOf(
            "--package",
            "Newtonsoft.Json",
            "--repository",
            "https://api.nuget.org/v3/index.json",
            "--test-mode",
            "false",
            "--output",
            "graph.png",
            "--filter",
            "System",
        )

    /**
     * Проверяет правильное чтение всех параметров.
     */
    @Test
    fun `parses valid arguments`() {
        val config = CliParser.parse(validArgs())

        assertEquals("Newtonsoft.Json", config.packageName)
        assertEquals("https://api.nuget.org/v3/index.json", config.repository)
        assertFalse(config.testMode)
        assertEquals("graph.png", config.outputFile)
        assertEquals("System", config.filter)
    }

    /**
     * Проверяет возможность запуска без фильтра.
     */
    @Test
    fun `allows omitted filter`() {
        val args = validArgs().dropLast(2).toTypedArray()
        val config = CliParser.parse(args)

        assertEquals("", config.filter)
    }

    /**
     * Проверяет реакцию на отсутствие имени пакета.
     */
    @Test
    fun `rejects missing package`() {
        val args = validArgs().drop(2).toTypedArray()

        assertThrows<IllegalArgumentException> {
            CliParser.parse(args)
        }
    }

    /**
     * Проверяет отклонение некорректного адреса репозитория.
     */
    @Test
    fun `rejects invalid repository URL`() {
        val args = validArgs()
        args[3] = "invalid-url"

        assertThrows<IllegalArgumentException> {
            CliParser.parse(args)
        }
    }

    /**
     * Проверяет недопустимое значение тестового режима.
     */
    @Test
    fun `rejects invalid test mode`() {
        val args = validArgs()
        args[5] = "maybe"

        assertThrows<IllegalArgumentException> {
            CliParser.parse(args)
        }
    }

    /**
     * Проверяет расширение выходного файла.
     */
    @Test
    fun `rejects invalid output extension`() {
        val args = validArgs()
        args[7] = "graph.txt"

        assertThrows<IllegalArgumentException> {
            CliParser.parse(args)
        }
    }

    /**
     * Проверяет обработку неизвестного параметра.
     */
    @Test
    fun `rejects unknown option`() {
        val args = validArgs() + arrayOf("--unknown", "value")

        assertThrows<IllegalArgumentException> {
            CliParser.parse(args)
        }
    }

    /**
     * Проверяет запрет повторного указания параметра.
     */
    @Test
    fun `rejects duplicate option`() {
        val args = validArgs() + arrayOf("--package", "Another")

        assertThrows<IllegalArgumentException> {
            CliParser.parse(args)
        }
    }

    /**
     * Проверяет отсутствие значения у известного параметра.
     */
    @Test
    fun `rejects missing option value`() {
        val args = validArgs() + arrayOf("--package")

        assertThrows<IllegalArgumentException> {
            CliParser.parse(args)
        }
    }

    /**
     * Проверяет работу с существующим тестовым файлом.
     */
    @Test
    fun `accepts existing test repository`() {
        val file = Files.createTempFile("repository-", ".txt")

        try {
            val args = validArgs()
            args[3] = file.toString()
            args[5] = "true"

            val config = CliParser.parse(args)

            assertTrue(config.testMode)
            assertEquals(file.toString(), config.repository)
        } finally {
            Files.deleteIfExists(file)
        }
    }

    /**
     * Проверяет отклонение отсутствующего тестового файла.
     */
    @Test
    fun `rejects missing test repository`() {
        val file = Files.createTempFile("repository-", ".txt")
        Files.delete(file)

        val args = validArgs()
        args[3] = file.toString()
        args[5] = "true"

        assertThrows<IllegalArgumentException> {
            CliParser.parse(args)
        }
    }
}
