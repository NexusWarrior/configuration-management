import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Проверяет извлечение зависимостей из NuGet nuspec.
 */
class NuspecParserTest {
    /**
     * Проверяет разбор нескольких целевых платформ.
     */
    @Test
    fun `parses dependency groups`() {
        val xml =
            """
            <package>
                <metadata>
                    <dependencies>
                        <group targetFramework="net8.0">
                            <dependency id="Example.Core" version="2.0.0"/>
                            <dependency id="Example.Utils" version="1.5.0"/>
                        </group>
                        <group targetFramework="net6.0">
                            <dependency id="Example.Legacy" version="1.0.0"/>
                        </group>
                    </dependencies>
                </metadata>
            </package>
            """.trimIndent()

        val groups = NuspecParser.parse(xml)

        assertEquals(2, groups.size)
        assertEquals("net8.0", groups[0].targetFramework)
        assertEquals(2, groups[0].dependencies.size)
        assertEquals("Example.Core", groups[0].dependencies[0].name)
        assertEquals("2.0.0", groups[0].dependencies[0].versionRange)
        assertEquals("net6.0", groups[1].targetFramework)
    }

    /**
     * Проверяет зависимости без указания платформы.
     */
    @Test
    fun `parses dependencies without groups`() {
        val xml =
            """
            <package>
                <metadata>
                    <dependencies>
                        <dependency id="Example.Core" version="1.0.0"/>
                    </dependencies>
                </metadata>
            </package>
            """.trimIndent()

        val groups = NuspecParser.parse(xml)

        assertEquals(1, groups.size)
        assertEquals("any", groups[0].targetFramework)
        assertEquals("Example.Core", groups[0].dependencies[0].name)
    }

    /**
     * Проверяет пакет без зависимостей.
     */
    @Test
    fun `handles package without dependencies`() {
        val xml =
            """
            <package>
                <metadata>
                    <id>Example</id>
                </metadata>
            </package>
            """.trimIndent()

        assertTrue(NuspecParser.parse(xml).isEmpty())
    }

    /**
     * Проверяет пустую группу целевой платформы.
     */
    @Test
    fun `handles empty dependency group`() {
        val xml =
            """
            <package>
                <metadata>
                    <dependencies>
                        <group targetFramework="net8.0"/>
                    </dependencies>
                </metadata>
            </package>
            """.trimIndent()

        val groups = NuspecParser.parse(xml)

        assertEquals(1, groups.size)
        assertTrue(groups[0].dependencies.isEmpty())
    }

    /**
     * Проверяет XML с пространством имён NuGet.
     */
    @Test
    fun `parses XML namespace`() {
        val xml =
            """
            <package xmlns="http://schemas.microsoft.com/packaging/2013/05/nuspec.xsd">
                <metadata>
                    <dependencies>
                        <group targetFramework="net8.0">
                            <dependency id="Example" version="1.0.0"/>
                        </group>
                    </dependencies>
                </metadata>
            </package>
            """.trimIndent()

        val groups = NuspecParser.parse(xml)

        assertEquals("Example", groups[0].dependencies[0].name)
    }

    /**
     * Проверяет защиту от внешних XML-сущностей.
     */
    @Test
    fun `rejects XML with DTD`() {
        val xml =
            """
            <!DOCTYPE package [
                <!ENTITY example SYSTEM "file:///etc/passwd">
            ]>
            <package>
                <metadata>
                    <id>&example;</id>
                </metadata>
            </package>
            """.trimIndent()

        assertThrows(Exception::class.java) {
            NuspecParser.parse(xml)
        }
    }

    /**
     * Проверяет ошибку при отсутствии metadata.
     */
    @Test
    fun `rejects missing metadata`() {
        assertThrows(IllegalArgumentException::class.java) {
            NuspecParser.parse("<package/>")
        }
    }
}
