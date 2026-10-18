
#!/usr/bin/env bash

# Завершаем выполнение при критических ошибках.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

# Собираем приложение перед тестированием.
./gradlew --quiet installDist

APP="./build/install/configuration-management/bin/configuration-management"
REPOSITORY="https://api.nuget.org/v3/index.json"

PASS=0
FAIL=0

# Проверяет успешный запуск и ожидаемую часть вывода.
check_success() {
    local name="$1"
    local expected="$2"
    shift 2

    local output
    local code=0

    output=$("$APP" "$@" 2>&1) || code=$?

    if [[ "$code" -eq 0 ]] && [[ "$output" == *"$expected"* ]]; then
        echo "УСПЕШНО: $name"
        PASS=$((PASS + 1))
    else
        echo "ОШИБКА: $name"
        echo "$output"
        FAIL=$((FAIL + 1))
    fi
}

# Проверяет, что приложение завершилось с ошибкой.
check_failure() {
    local name="$1"
    shift

    local output
    local code=0

    output=$("$APP" "$@" 2>&1) || code=$?

    if [[ "$code" -ne 0 ]] && [[ "$output" == *"Ошибка:"* ]]; then
        echo "УСПЕШНО: $name"
        PASS=$((PASS + 1))
    else
        echo "ОШИБКА: $name"
        echo "$output"
        FAIL=$((FAIL + 1))
    fi
}

# Проверяем получение реальных зависимостей NuGet.
check_success "Получение пакета Newtonsoft.Json" \
    "Пакет: Newtonsoft.Json" \
    --package Newtonsoft.Json \
    --repository "$REPOSITORY" \
    --test-mode false \
    --output graph.png

# Проверяем наличие раздела зависимостей.
check_success "Получение прямых зависимостей" \
    "Прямые зависимости:" \
    --package Serilog.AspNetCore \
    --repository "$REPOSITORY" \
    --test-mode false \
    --output graph.png

# Проверяем некорректный адрес репозитория.
check_failure "Некорректный URL" \
    --package Newtonsoft.Json \
    --repository invalid-url \
    --test-mode false \
    --output graph.png

# Проверяем отсутствие обязательных параметров.
check_failure "Отсутствуют параметры"

# Выводим сводные результаты.
echo
echo "Успешно: $PASS"
echo "Ошибок: $FAIL"

if [[ "$FAIL" -ne 0 ]]; then
    exit 1
fi
