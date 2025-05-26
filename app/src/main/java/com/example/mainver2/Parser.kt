package com.example.mainver2

sealed class Block {
    data class Init(val varName: String) : Block()
    data class Assign(val varName: String, val rpn: List<String>) : Block()
    data class Arithmetic(val varName: String, val rpn: List<String>) : Block()
    data class Compare(val varName: String, val rpn: List<String>) : Block()
    data class If(val varName: String, val rpn: List<String>) : Block()
    object Else : Block()
    data class While(val varName: String, val rpn: List<String>) : Block()
    data class InitArray(val arrayName: String, val size: Int) : Block()
    data class AssignArray(val arrayName: String, val index: Int, val rpn: List<String>) : Block()
    data class GetArray(val varName: String, val arrayName: String, val index: Int) : Block()
    object Unknown : Block()
}

class Parser {

    private val precedence = mapOf(
        "||" to 1,
        "&&" to 2,
        "==" to 3, "!=" to 3,
        "<" to 4, ">" to 4, "<=" to 4, ">=" to 4,
        "+" to 5, "-" to 5,
        "*" to 6, "/" to 6, "%" to 6
    )

    private fun isOperator(token: String): Boolean = precedence.containsKey(token)

    private fun infixToRPN(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = ArrayDeque<String>()

        for (token in tokens) {
            when {
                token.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*")) || token.matches(Regex("\\d+")) -> {
                    output.add(token)
                }
                isOperator(token) -> {
                    while (stack.isNotEmpty() && isOperator(stack.last())
                        && precedence[token]!! <= precedence[stack.last()]!!) {
                        output.add(stack.removeLast())
                    }
                    stack.add(token)
                }
                token == "(" -> stack.add(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.last() != "(") {
                        output.add(stack.removeLast())
                    }
                    if (stack.isEmpty() || stack.last() != "(") {
                        return listOf("ERROR: mismatched parentheses")
                    }
                    stack.removeLast()
                }
                else -> return listOf("ERROR: unknown token '$token'")
            }
        }

        while (stack.isNotEmpty()) {
            val op = stack.removeLast()
            if (op == "(" || op == ")") {
                return listOf("ERROR: mismatched parentheses")
            }
            output.add(op)
        }

        return output
    }

    private fun tokenize(expr: String): List<String> {
        val spaced = expr
            .replace(Regex("([()])"), " $1 ")
            .replace("&&", " && ")
            .replace("||", " || ")
            .replace(">=", " >= ")
            .replace("<=", " <= ")
            .replace("==", " == ")
            .replace("!=", " != ")
            .replace(Regex("(?<=[^<>!=])([<>])(?=[^=])"), " $1 ")
            .replace(Regex("(?<=[^!<>=])([=])(?=[^=])"), " $1 ")
        return spaced.trim().split(Regex("\\s+"))
    }

    fun parse(input: String): Block {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return Block.Unknown

        val commandParts = trimmed.split(" ", limit = 2)
        val command = commandParts[0]
        val rest = if (commandParts.size > 1) commandParts[1] else ""

        return when (command) {
            "init" -> parseInit(rest)
            "assign" -> parseAssign(rest)
            "arithmetic" -> parseArithmetic(rest)
            "compare" -> parseCompare(rest)
            "if" -> parseIf(rest)
            "while" -> parseWhile(rest)
            "else" -> Block.Else
            "initArray" -> parseInitArray(rest)
            "assignArray" -> parseAssignArray(rest)
            "getArray" -> parseGetArray(rest)
            else -> Block.Unknown
        }
    }

    private fun parseInit(rest: String): Block {
        val varName = rest.trim()
        return if (varName.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*"))) Block.Init(varName)
        else Block.Unknown
    }

    private fun parseAssign(rest: String): Block {
        val parts = rest.trim().split(" ", limit = 2)
        if (parts.size < 2) return Block.Unknown
        val varName = parts[0]
        val expr = parts[1]
        val rpn = infixToRPN(tokenize(expr))
        return Block.Assign(varName, rpn)
    }

    private fun parseArithmetic(rest: String): Block {
        val parts = rest.trim().split(" ", limit = 2)
        if (parts.size < 2) return Block.Unknown
        val varName = parts[0]
        val expr = parts[1]
        val rpn = infixToRPN(tokenize(expr))
        return Block.Arithmetic(varName, rpn)
    }

    private fun parseCompare(rest: String): Block {
        val parts = rest.trim().split(" ", limit = 2)
        if (parts.size < 2) return Block.Unknown
        val varName = parts[0]
        val expr = parts[1]
        val rpn = infixToRPN(tokenize(expr))
        return Block.Compare(varName, rpn)
    }

    private fun parseIf(rest: String): Block {
        val parts = rest.trim().split(" ", limit = 2)
        if (parts.size < 2) return Block.Unknown
        val varName = parts[0]
        val expr = parts[1]
        val rpn = infixToRPN(tokenize(expr))
        return Block.If(varName, rpn)
    }

    private fun parseWhile(rest: String): Block {
        val parts = rest.trim().split(" ", limit = 2)
        if (parts.size < 2) return Block.Unknown
        val varName = parts[0]
        val expr = parts[1]
        val rpn = infixToRPN(tokenize(expr))
        return Block.While(varName, rpn)
    }

    private fun parseInitArray(rest: String): Block {
        val parts = rest.trim().split(" ")
        if (parts.size != 2) return Block.Unknown
        val name = parts[0]
        val size = parts[1].toIntOrNull() ?: return Block.Unknown
        return Block.InitArray(name, size)
    }

    private fun parseAssignArray(rest: String): Block {
        val parts = rest.trim().split(" ", limit = 3)
        if (parts.size < 3) return Block.Unknown
        val name = parts[0]
        val index = parts[1].toIntOrNull() ?: return Block.Unknown
        val expr = parts[2]
        val rpn = infixToRPN(tokenize(expr))
        return Block.AssignArray(name, index, rpn)
    }

    private fun parseGetArray(rest: String): Block {
        val parts = rest.trim().split(" ")
        if (parts.size != 3) return Block.Unknown
        val varName = parts[0]
        val arrayName = parts[1]
        val index = parts[2].toIntOrNull() ?: return Block.Unknown
        return Block.GetArray(varName, arrayName, index)
    }
}