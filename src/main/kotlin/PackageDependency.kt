/**
 * Описывает прямую зависимость NuGet-пакета.
 *
 * @property name Имя зависимого пакета.
 * @property versionRange Допустимый диапазон версий зависимости.
 */
data class PackageDependency(
    val name: String,
    val versionRange: String,
)

/**
 * Хранит зависимости для одной целевой платформы .NET.
 *
 * @property targetFramework Название целевой платформы.
 * @property dependencies Список прямых зависимостей.
 */
data class DependencyGroup(
    val targetFramework: String,
    val dependencies: List<PackageDependency>,
)

/**
 * Хранит результат получения зависимостей NuGet-пакета.
 *
 * @property name Имя анализируемого пакета.
 * @property version Выбранная стабильная версия пакета.
 * @property groups Группы зависимостей по целевым платформам.
 */
data class PackageInfo(
    val name: String,
    val version: String,
    val groups: List<DependencyGroup>,
)
