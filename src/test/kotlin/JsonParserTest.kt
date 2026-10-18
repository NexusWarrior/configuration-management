import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Модульные тесты для собственного JSON-парсера.
 */
class JsonParserTest {
    /**
     * Проверяет разбор массива доступных версий пакета.
     */
    @Test
    fun `parses package versions`() {
        val json =
            """
            {
                "versions": ["12.0.3", "13.0.1", "13.0.4"]
            }
            """.trimIndent()

        val result = JsonParser(json).parse() as Map<*, *>
        val versions = result["versions"] as List<*>

        assertEquals(3, versions.size)
        assertEquals("13.0.4", versions.last())
    }

    /**
     * Проверяет чтение ресурса из Service Index.
     */
    @Test
    fun `parses service index`() {
        val json =
            """
            {
                "resources": [
                    {
                        "@id": "https://example.org/flat",
                        "@type": "PackageBaseAddress/3.0.0"
                    }
                ]
            }
            """.trimIndent()

        val root = JsonParser(json).parse() as Map<*, *>
        val resources = root["resources"] as List<*>
        val resource = resources.first() as Map<*, *>

        assertEquals(
            "PackageBaseAddress/3.0.0",
            resource["@type"],
        )
    }

    /**
     * Проверяет специальные символы в строках.
     */
    @Test
    fun `parses escaped characters`() {
        val result = JsonParser(""""Hello\nWorld"""").parse()

        assertEquals("Hello\nWorld", result)
    }

    /**
     * Проверяет логические значения и null.
     */
    @Test
    fun `parses primitive values`() {
        assertEquals(true, JsonParser("true").parse())
        assertEquals(false, JsonParser("false").parse())
        assertNull(JsonParser("null").parse())
        assertEquals(42.0, JsonParser("42").parse())
    }

    /**
     * Проверяет отказ при повреждённом JSON.
     */
    @Test
    fun `rejects invalid JSON`() {
        assertThrows(IllegalArgumentException::class.java) {
            JsonParser("""{"versions": [}""").parse()
        }
    }

    /**
     * Проверяет отсутствие лишних символов после документа.
     */
    @Test
    fun `rejects trailing content`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                JsonParser("""{"name":"test"} extra""").parse()
            }

        assertTrue(exception.message!!.contains("Лишние символы"))
    }
}
