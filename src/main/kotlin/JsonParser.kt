/**
 * Разбирает JSON без сторонних библиотек.
 *
 * Возвращает объекты как Map, массивы как List,
 * а простые значения как стандартные типы Kotlin.
 */
class JsonParser(
    private val source: String,
) {
    private var position = 0

    /**
     * Разбирает JSON-документ целиком.
     */
    fun parse(): Any? {
        val value = readValue()
        skipWhitespace()
        require(position == source.length) {
            "Лишние символы после JSON"
        }
        return value
    }

    /**
     * Определяет тип следующего значения.
     */
    private fun readValue(): Any? {
        skipWhitespace()
        require(position < source.length) {
            "Неожиданный конец JSON"
        }

        return when (source[position]) {
            '{' -> readObject()
            '[' -> readArray()
            '"' -> readString()
            't' -> readLiteral("true", true)
            'f' -> readLiteral("false", false)
            'n' -> readLiteral("null", null)
            else -> readNumber()
        }
    }

    /**
     * Разбирает JSON-объект.
     */
    private fun readObject(): Map<String, Any?> {
        expect('{')
        val result = mutableMapOf<String, Any?>()
        skipWhitespace()

        if (consume('}')) return result

        do {
            skipWhitespace()
            val key = readString()
            expect(':')
            result[key] = readValue()
            skipWhitespace()

            if (consume('}')) return result
            expect(',')
        } while (true)
    }

    /**
     * Разбирает JSON-массив.
     */
    private fun readArray(): List<Any?> {
        expect('[')
        val result = mutableListOf<Any?>()
        skipWhitespace()

        if (consume(']')) return result

        do {
            result.add(readValue())
            skipWhitespace()

            if (consume(']')) return result
            expect(',')
        } while (true)
    }

    /**
     * Читает строку с обработкой управляющих символов.
     */
    private fun readString(): String {
        expect('"')
        val result = StringBuilder()

        while (position < source.length) {
            val char = source[position++]

            when (char) {
                '"' -> {
                    return result.toString()
                }

                '\\' -> {
                    result.append(readEscape())
                }

                else -> {
                    require(char >= ' ') {
                        "Недопустимый символ внутри строки JSON"
                    }
                    result.append(char)
                }
            }
        }

        throw IllegalArgumentException("Незакрытая строка JSON")
    }

    /**
     * Обрабатывает экранированный символ.
     */
    private fun readEscape(): Char {
        require(position < source.length) {
            "Незавершённая escape-последовательность"
        }

        return when (source[position++]) {
            '"' -> '"'

            '\\' -> '\\'

            '/' -> '/'

            'b' -> '\b'

            'f' -> '\u000C'

            'n' -> '\n'

            'r' -> '\r'

            't' -> '\t'

            'u' -> readUnicode()

            else -> throw IllegalArgumentException(
                "Неизвестная escape-последовательность",
            )
        }
    }

    /**
     * Читает Unicode-символ в формате uXXXX.
     */
    private fun readUnicode(): Char {
        require(position + 4 <= source.length) {
            "Некорректный Unicode в JSON"
        }

        val digits = source.substring(position, position + 4)
        val code =
            digits.toIntOrNull(16)
                ?: throw IllegalArgumentException("Некорректный Unicode")

        position += 4
        return code.toChar()
    }

    /**
     * Читает числовое значение JSON.
     */
    private fun readNumber(): Double {
        val start = position

        while (position < source.length) {
            val char = source[position]

            if (char in "0123456789+-.eE") {
                position++
            } else {
                break
            }
        }

        require(position > start) {
            "Ожидалось значение JSON"
        }

        val text = source.substring(start, position)
        val numberFormat =
            Regex(
                "-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?",
            )

        require(numberFormat.matches(text)) {
            "Некорректное число JSON"
        }

        return text.toDouble()
    }

    /**
     * Читает фиксированное значение JSON.
     */
    private fun readLiteral(
        expected: String,
        value: Any?,
    ): Any? {
        require(source.startsWith(expected, position)) {
            "Некорректное значение JSON"
        }

        position += expected.length
        return value
    }

    /**
     * Пропускает пробельные символы.
     */
    private fun skipWhitespace() {
        while (position < source.length && source[position] in " \n\r\t") {
            position++
        }
    }

    /**
     * Проверяет наличие ожидаемого символа.
     */
    private fun expect(char: Char) {
        skipWhitespace()

        require(consume(char)) {
            "Ожидался символ '$char' в позиции $position"
        }
    }

    /**
     * Проверяет символ и перемещает позицию чтения.
     */
    private fun consume(char: Char): Boolean {
        if (position < source.length && source[position] == char) {
            position++
            return true
        }
        return false
    }
}
