package com.example.mainver2

class Program {

//    var program: MutableList<() -> Any?> = mutableListOf() //program.add { compare(1, 2, "<=") }
    var vars: MutableMap<String, Double> = mutableMapOf()
    var arrays: MutableMap<String, MutableList<Double>> = mutableMapOf()

    fun initializeVar(name: String) {
        vars[name] = 0.0
    }
    fun initializeArray(name: String, body: MutableList<Double>) {
        arrays[name] = body
    }
    fun assignArray(name: String, index: Int, value: Double) {
        arrays[name]!![index] = value
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

}