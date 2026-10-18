
#!/usr/bin/env bash

# Прекращаем выполнение при ошибках и использовании
# необъявленных переменных.
set -euo pipefail

# Определяем каталог, в котором расположен скрипт.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Переходим в корневой каталог проекта.
cd "$SCRIPT_DIR"

# Проверяем наличие Gradle Wrapper.
if [[ ! -f "./gradlew" ]]; then
    echo "Ошибка: Gradle Wrapper не найден" >&2
    exit 1
fi

# Собираем дистрибутив приложения.
./gradlew --quiet installDist

# Запускаем приложение с исходными аргументами.
# Конструкция "$@" сохраняет пробелы внутри значений.
exec "./build/install/configuration-management/bin/configuration-management" "$@"
