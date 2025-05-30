// Определение типов блоков DSL (AST)
sealed class Block {
    data class Init(val name: String) : Block()
    // Для assign храним RPN-строку, где вместо "=" используется слово "assign"
    data class Assign(val rpn: String) : Block()
    data class InitArray(val name: String, val size: Int, val elements: List<Int>? = null) : Block()
    data class AssignArray(val name: String, val index: String, val rpn: String) : Block()
    // Условия if-else: условие хранится в виде RPN-строки, thenBody – список блоков then, elseBody – список блоков else
    data class IfElse(val condition: String, val thenBody: List<Block>, val elseBody: List<Block>) : Block()
    // Цикл while: условие – RPN-строка, body – список блоков
    data class While(val condition: String, val body: List<Block>) : Block()
    data class Print(val value: String) : Block() // Новая команда print
    object Unknown : Block()
}

// Парсер DSL
class Parser {
    private var currentIndex = 0
    private lateinit var lines: List<String>

    // Приоритет операторов (оператор присваивания "=" имеет самый низкий приоритет)
    private val precedence = mapOf(
        "=" to 0,
        "||" to 1,
        "&&" to 2,
        "==" to 3, "!=" to 3,
        "<" to 4, ">" to 4, "<=" to 4, ">=" to 4,
        "+" to 5, "-" to 5,
        "*" to 6, "/" to 6, "%" to 6
    )

    // Определяем, является ли оператор справаассоциативным (только "=" считается правосторонним)
    private fun isRightAssociative(op: String): Boolean = op == "="
    private fun isOperator(token: String) = precedence.containsKey(token)

    // Токенизация выражения: выделяет пробелами операторы, скобки и т.д.
    private fun tokenize(expr: String): List<String> {
        return expr
            .replace("(", " ( ")
            .replace(")", " ) ")
            .replace("&&", " && ")
            .replace("||", " || ")
            .replace(">=", " >= ")
            .replace("<=", " <= ")
            .replace("==", " == ")
            .replace("!=", " != ")
            .replace("=", " = ")
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
    }

    // Алгоритм сортировочной станции для преобразования инфиксного выражения в обратную польскую запись (RPN).
    private fun infixToRpn(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val opStack = mutableListOf<String>()
        for (token in tokens) {
            when {
                token.matches(Regex("\\d+")) || token.matches(Regex("[a-zA-Z_]+")) ->
                    output.add(token)
                isOperator(token) -> {
                    while (opStack.isNotEmpty() && isOperator(opStack.last())) {
                        if (isRightAssociative(token)) {
                            if (precedence[opStack.last()]!! > precedence[token]!!)
                                output.add(opStack.removeAt(opStack.size - 1))
                            else break
                        } else {
                            if (precedence[opStack.last()]!! >= precedence[token]!!)
                                output.add(opStack.removeAt(opStack.size - 1))
                            else break
                        }
                    }
                    opStack.add(token)
                }
                token == "(" -> opStack.add(token)
                token == ")" -> {
                    while (opStack.isNotEmpty() && opStack.last() != "(") {
                        output.add(opStack.removeAt(opStack.size - 1))
                    }
                    if (opStack.isNotEmpty() && opStack.last() == "(")
                        opStack.removeAt(opStack.size - 1)
                }
                else -> { /* игнорируем неизвестные токены */ }
            }
        }
        while (opStack.isNotEmpty())
            output.add(opStack.removeAt(opStack.size - 1))
        return output
    }

    // Преобразование инфиксного выражения в RPN-строку: токены объединяются через пробел.
    // Если последний токен равен "=" (оператор присваивания), мы заменяем его на слово "assign".
    private fun infixExprToRpnString(expr: String): String {
        val tokens = tokenize(expr)
        val rpnTokens = infixToRpn(tokens).toMutableList()
        if (rpnTokens.isNotEmpty() && rpnTokens.last() == "=") {
            rpnTokens[rpnTokens.lastIndex] = "assign"
        }
        return rpnTokens.joinToString(" ")
    }

    // Основная функция парсера: разбивает входной текст на строки, удаляет завершающие ";" и проводит разбор.
    fun parseProgram(input: String): List<Block> {
        lines = input.lines().map { it.trim().removeSuffix(";") }.filter { it.isNotEmpty() }
        currentIndex = 0
        return parseBlockSequence()
    }

    // Рекурсивная функция разбора последовательностей команд, включая поддержку блоков с фигурными скобками.
    private fun parseBlockSequence(): List<Block> {
        val blocks = mutableListOf<Block>()
        while (currentIndex < lines.size) {
            val line = lines[currentIndex].trim()
            if (line == "}") {
                currentIndex++ // Завершение блока
                break
            }
            if (line.startsWith("if")) {
                blocks.add(parseIfElse())
            } else if (line.startsWith("while")) {
                blocks.add(parseWhile())
            } else {
                blocks.add(parseLine(line))
                currentIndex++
            }
        }
        return blocks
    }

    // Разбор конструкции if-else.
    // Ожидается формат:
    //   if (условие) { ... }
    //   else { ... }
    private fun parseIfElse(): Block {
        val ifLine = lines[currentIndex]
        currentIndex++
        val condition = extractCondition(ifLine, "if")
        val conditionRpn = infixExprToRpnString(condition)
        val thenBody = parseBlockSequence() // Читаем блок then
        var elseBody = listOf<Block>()
        if (currentIndex < lines.size) {
            val nextLine = lines[currentIndex].trim()
            if (nextLine.startsWith("else")) {
                currentIndex++ // Читаем строку else
                elseBody = parseBlockSequence() // Читаем блок else
            }
        }
        return Block.IfElse(conditionRpn, thenBody, elseBody)
    }

    // Разбор конструкции while.
    private fun parseWhile(): Block {
        val whileLine = lines[currentIndex]
        currentIndex++
        val condition = extractCondition(whileLine, "while")
        val conditionRpn = infixExprToRpnString(condition)
        val body = parseBlockSequence()
        return Block.While(conditionRpn, body)
    }

    // Разбор одиночной команды: init, assign, initArray, assignArray.
    private fun parseLine(line: String): Block {
        val parts = line.split(Regex("\\s+"), limit = 2)
        val command = parts[0]
        val rest = if (parts.size > 1) parts[1] else ""
        return when (command) {
            "init" -> Block.Init(rest)
            "assign" -> Block.Assign(infixExprToRpnString(rest))
            "initArray", "intArray" -> {
                val expr = rest.replace("=", "").trim()
                val parts2 = expr.split(Regex("\\s+"), limit = 2)
                if (parts2.size < 2) Block.Unknown
                else {
                    val name = parts2[0]
                    if (parts2[1].startsWith("{") && parts2[1].endsWith("}")) {
                        val elems = parts2[1].removePrefix("{").removeSuffix("}")
                            .split(",").mapNotNull { it.trim().toIntOrNull() }
                        Block.InitArray(name, elems.size, elems)
                    } else {
                        val size = parts2[1].toIntOrNull() ?: return Block.Unknown
                        Block.InitArray(name, size)
                    }
                }
            }
            "assignArray" -> {
                val expr = rest.replace("=", "").trim()
                val parts2 = expr.split(Regex("\\s+"), limit = 3)
                if (parts2.size < 3) Block.Unknown
                else Block.AssignArray(parts2[0], parts2[1], infixExprToRpnString(parts2[2]))
            }
            "print" -> Block.Print(rest) // Добавляем обработку print
            else -> Block.Unknown
        }
    }

    // Извлечение условия из строки вида "if (condition) {" или "while (condition) {"
    private fun extractCondition(line: String, keyword: String): String {
        val start = line.indexOf("(")
        val end = line.indexOf(")")
        return if (start != -1 && end != -1 && end > start)
            line.substring(start + 1, end).trim()
        else ""
    }

    // Компиляция (объединение) AST в единую строку RPN.
    fun compileProgramToRpnString(blocks: List<Block>): String {
        return blocks.joinToString(" ") { blockToRpn(it) }
    }

    private fun blockToRpn(block: Block): String = when (block) {
        is Block.Init -> "init ${block.name}"
        is Block.Assign -> block.rpn
        is Block.InitArray ->
            if (block.elements != null)
                "intArray ${block.name} {${block.elements.joinToString(" ")}}"
            else "intArray ${block.name} ${block.size}"
        is Block.AssignArray -> "assignArray ${block.name} ${block.index} ${block.rpn}"
        is Block.While -> "while ${block.condition} { ${block.body.joinToString(" ") { blockToRpn(it) }} }"
        is Block.IfElse -> "if ${block.condition} { ${block.thenBody.joinToString(" ") { blockToRpn(it) }} } else { ${block.elseBody.joinToString(" ") { blockToRpn(it) }} }"
        is Block.Print -> "print ${block.value}" // Добавляем команду print в RPN
        else -> ""
    }
}

