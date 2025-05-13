package com.example.mainver2

class program {

    var program: MutableList<() -> Any?> = mutableListOf() //program.add { compare(1, 2, "<=") }
    var vars: MutableMap<String, Int> = mutableMapOf()

    fun initializeVar(name: String) {
        vars[name] = 0
    }
    fun assignVar(name: String, value: Int) {
        vars[name] = value
    }
    fun compare(a: Int, b: Int, mode: String): Boolean{
        var ans = true
        if (mode == "<") {
            if (a >= b) {
                ans = false
            }
        }
        if (mode == ">") {
            if (a <= b) {
                ans = false
            }
        }
        if (mode == "<=") {
            if (a > b) {
                ans = false
            }
        }
        if (mode == ">=") {
            if (a < b) {
                ans = false
            }
        }
        if (mode == "==") {
            if (a != b) {
                ans = false
            }
        }
        if (mode == "!=") {
            if (a == b) {
                ans = false
            }
        }
        return ans
    }
    fun arithmetic(a: Int, b: Int, mode: String): Int{
        if (mode == "+") {
            return a + b
        }
        if (mode == "-") {
            return a - b
        }
        if (mode == "/") {
            return a / b
        }
        if (mode == "*") {
            return a * b
        }
        if (mode == "%") {
            return a % b
        }
        return -1
    }
    fun condition(a: Int, b: Int, mode: String, commandsIf: MutableList<() -> Any?>, commandsElse: MutableList<() -> Any?>) {
        if (compare(a, b, mode)) {
            runProgram(commandsIf)
        }
        else {
            runProgram(commandsElse)
        }
    }
    fun runWhile(commands: MutableList<() -> Any?>, a: Int, b: Int, mode: String) { //пока нельзя while true и без break'ов
        while (true) {
            if (compare(a, b, mode)) {
                runProgram(commands)
            }
            else {
                break
            }
        }
    }
    fun runProgram(commands: MutableList<() -> Any?>) {
        commands.forEach {command -> command()}
    }
}