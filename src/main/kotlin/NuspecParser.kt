import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Извлекает прямые зависимости из XML-манифеста NuGet.
 */
object NuspecParser {
    /**
     * Возвращает группы зависимостей из документа nuspec.
     *
     * @param xml Содержимое XML-манифеста.
     */
    fun parse(xml: String): List<DependencyGroup> {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true

        // Запрещаем внешние сущности и DTD для защиты XML-парсера.
        factory.setFeature(
            "http://apache.org/xml/features/disallow-doctype-decl",
            true,
        )
        factory.setFeature(
            XMLConstants.FEATURE_SECURE_PROCESSING,
            true,
        )
        factory.setFeature(
            "http://xml.org/sax/features/external-general-entities",
            false,
        )
        factory.setFeature(
            "http://xml.org/sax/features/external-parameter-entities",
            false,
        )
        factory.setXIncludeAware(false)
        factory.isExpandEntityReferences = false
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")

        val builder = factory.newDocumentBuilder()
        val bytes = xml.toByteArray(Charsets.UTF_8)

        val document =
            ByteArrayInputStream(bytes).use {
                builder.parse(it)
            }

        val root = document.documentElement
        require(root.localName == "package") {
            "Некорректный корневой элемент nuspec"
        }

        val metadata =
            children(root, "metadata").firstOrNull()
                ?: throw IllegalArgumentException("В nuspec отсутствует metadata")

        val dependencies =
            children(metadata, "dependencies").firstOrNull()
                ?: return emptyList()

        return readGroups(dependencies)
    }

    /**
     * Получает группы зависимостей для разных платформ.
     */
    private fun readGroups(element: Element): List<DependencyGroup> {
        val groups = children(element, "group")

        if (groups.isEmpty()) {
            val dependencies = readDependencies(element)
            return if (dependencies.isEmpty()) {
                emptyList()
            } else {
                listOf(DependencyGroup("any", dependencies))
            }
        }

        val result =
            groups.map { group ->
                val framework =
                    group
                        .getAttribute("targetFramework")
                        .ifBlank { "any" }

                DependencyGroup(framework, readDependencies(group))
            }

        val direct = readDependencies(element)
        return if (direct.isEmpty()) {
            result
        } else {
            result + DependencyGroup("any", direct)
        }
    }

    /**
     * Извлекает прямые зависимости указанного XML-элемента.
     */
    private fun readDependencies(element: Element): List<PackageDependency> =
        children(element, "dependency").map { dependency ->
            val name = dependency.getAttribute("id")
            val version = dependency.getAttribute("version")

            require(name.isNotBlank()) {
                "Зависимость без имени в nuspec"
            }

            PackageDependency(name, version)
        }

    /**
     * Возвращает непосредственные дочерние элементы с нужным именем.
     */
    private fun children(
        element: Element,
        name: String,
    ): List<Element> {
        val result = mutableListOf<Element>()
        val nodes = element.childNodes

        for (index in 0 until nodes.length) {
            val node = nodes.item(index)

            if (node.nodeType == Node.ELEMENT_NODE && node.localName == name) {
                result.add(node as Element)
            }
        }

        return result
    }
}
