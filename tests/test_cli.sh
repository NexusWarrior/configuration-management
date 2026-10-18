
#!/usr/bin/env bash

# Останавливаем скрипт при критических ошибках.
set -euo pipefail

# Определяем корневую папку проекта.
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

# Счётчики успешных и неуспешных проверок.
PASS=0
FAIL=0

# Собираем приложение один раз перед проверками.
./gradlew --quiet installDist

# Путь к исполняемому скрипту приложения.
APP="./build/install/configuration-management/bin/configuration-management"

# Запускает сценарий и проверяет код завершения.
run_test() {
    local name="$1"
    local expected_exit="$2"
    shift 2

    local result
    local actual_exit=0

    # Получаем вывод приложения и код завершения.
    result=$("$APP" "$@" 2>&1) || actual_exit=$?

    if [[ "$actual_exit" -eq "$expected_exit" ]]; then
        echo "УСПЕШНО: $name"
        PASS=$((PASS + 1))
    else
        echo "ОШИБКА: $name"
        echo "Ожидаемый код: $expected_exit"
        echo "Фактический код: $actual_exit"
        echo "$result"
        FAIL=$((FAIL + 1))
    fi
}

# Проверяем запуск с корректными параметрами.
run_test "Корректные параметры" 0 \
    --package Newtonsoft.Json \
    --repository https://api.nuget.org/v3/index.json \
    --test-mode false \
    --output graph.png \
    --filter System

# Проверяем запуск без обязательных параметров.
run_test "Отсутствие параметров" 1

# Проверяем ошибочное значение тестового режима.
run_test "Некорректный тестовый режим" 1 \
    --package Newtonsoft.Json \
    --repository https://api.nuget.org/v3/index.json \
    --test-mode maybe \
    --output graph.png

# Проверяем недопустимое расширение файла.
run_test "Неверное расширение файла" 1 \
    --package Newtonsoft.Json \
    --repository https://api.nuget.org/v3/index.json \
    --test-mode false \
    --output graph.txt

# Выводим результаты тестирования.
echo
echo "Успешно: $PASS"
echo "Ошибок: $FAIL"

# Возвращаем ошибку, если хотя бы один тест не прошёл.
if [[ "$FAIL" -ne 0 ]]; then
    exit 1
fi
