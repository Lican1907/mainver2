package com.example.mainver2

class Program {

//    var program: MutableList<() -> Any?> = mutableListOf() //program.add { compare(1, 2, "<=") }
    var vars: MutableMap<String, Double> = mutableMapOf()

    fun initializeVar(name: String) {
        vars[name] = 0.0
    }
    fun assignVar(name: String, value: Double) {
        vars[name] = value
    }
    fun compare(a: Double, b: Double, mode: String): Boolean{
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
    fun arithmetic(a: Double, b: Double, mode: String): Double{
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
        return -1.0
    }
    fun condition(a: Double, b: Double, mode: String, commandsIf: MutableList<() -> Any?>, commandsElse: MutableList<() -> Any?>) {
        if (compare(a, b, mode)) {
            runProgram(commandsIf)
        }
        else {
            runProgram(commandsElse)
        }
    }
    fun runWhile(commands: MutableList<() -> Any?>, a: Double, b: Double, mode: String) { //пока нельзя while true и без break'ов
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