
# Визуализатор зависимостей NuGet

Практическая работа №2 по дисциплине
«Конфигурационное управление».

Вариант №24.

## Описание

Консольное приложение на Kotlin для анализа
зависимостей пакетов .NET (NuGet).

Проект разрабатывается поэтапно. В окончательной
версии приложение будет получать зависимости
пакетов и визуализировать их с помощью Graphviz.

## Технологии

- Kotlin
- Java 21
- Gradle
- JUnit 5
- Bash
- Graphviz (на этапе 5)

## Этапы разработки

1. Минимальный прототип с конфигурацией.
2. Получение прямых зависимостей NuGet.
3. Построение графа алгоритмом DFS с рекурсией.
4. Поиск обратных зависимостей.
5. Визуализация графа в формате PNG.

## Возможности первого этапа

- Чтение параметров командной строки.
- Проверка корректности параметров.
- Вывод конфигурации в формате ключ=значение.
- Обработка ошибок с ненулевым кодом завершения.

На первом этапе приложение ещё не обращается
к NuGet и не создаёт изображение графа.

## Параметры командной строки

| Параметр | Описание |
|---|---|
| `--package` | Имя анализируемого пакета |
| `--repository` | URL репозитория или путь к тестовому файлу |
| `--test-mode` | Тестовый режим: true или false |
| `--output` | Имя выходного PNG-файла |
| `--filter` | Подстрока для исключения пакетов |

Параметр `--filter` можно не указывать.
В таком случае используется пустая строка.

## Запуск в Linux и macOS

Для запуска используется Gradle Wrapper.

```bash
chmod +x gradlew run.sh
./run.sh \
  --package Newtonsoft.Json \
  --repository https://api.nuget.org/v3/index.json \
  --test-mode false \
  --output graph.png \
  --filter System
```

## Запуск в Windows

В терминале PowerShell:

```powershell
.\gradlew.bat run --args="--package Newtonsoft.Json --repository https://api.nuget.org/v3/index.json --test-mode false --output graph.png --filter System"
```

## Пример результата

```text
package=Newtonsoft.Json
repository=https://api.nuget.org/v3/index.json
test-mode=false
output=graph.png
filter=System
```

## Запуск модульных тестов

В Linux и macOS:

```bash
./gradlew clean test
```

В Windows:

```powershell
.\gradlew.bat clean test
```

## Интеграционные тесты

Для Linux и macOS:

```bash
chmod +x tests/test_cli.sh
./tests/test_cli.sh
```

Тестируются успешный запуск и различные
варианты ошибочных параметров.

## Структура проекта

- `src/main/kotlin` — исходный код приложения.
- `src/test/kotlin` — модульные тесты JUnit 5.
- `tests` — интеграционные Bash-тесты.
- `run.sh` — скрипт запуска приложения.
- `build.gradle.kts` — конфигурация сборки.
