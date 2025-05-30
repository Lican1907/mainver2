package com.example.mainver2

class Interpreter {
    var iterator: Int = 0
    var program: Program
    var reversePolishString: MutableList<String>
    var buffer: MutableList<String> = mutableListOf()

    constructor(reversePolishString: MutableList<String>) {
        this.reversePolishString = reversePolishString
        this.program = Program()
    }

    private fun popElement(): String {
        if (reversePolishString.isEmpty()) {
            return "endOfProgram"
        }
        else {
            val a: String = reversePolishString[0]
            reversePolishString.removeAt(0)
            return a
        }
    }
    private fun isNum(el: String): Boolean {
        return el.toDoubleOrNull() != null
    }
    private fun isVar(el: String): Boolean {
        return el in program.vars
    }
    private fun isArray(el: String): Boolean {
        return el in program.arrays
    }
    private fun isAriphmSign(el: String): Boolean {
        return el in setOf("+", "-", "/", "*", "%")
    }
    private fun isCompareSign(el: String): Boolean {
        return el in setOf(">", "<", "<=", ">=", "==", "!=")
    }
    private fun searchElse() {
        var counter: Int = 1
        while (reversePolishString[iterator] != "else" && counter != 0) {
            iterator +=1
            if (reversePolishString[iterator] == "if") counter++
            if (reversePolishString[iterator] == "else") counter--
        }
    }
    private fun searchCloseBracket() {
        var counter: Int = 1
        while (reversePolishString[iterator] != "}" && counter != 0) {
            iterator +=1
            if (reversePolishString[iterator] == "{") counter++
            if (reversePolishString[iterator] == "}") counter--
        }
    }
    private fun runIf() {
        iterator += 1
        readReversePolishString()
        if (buffer[0].toBoolean()) {
            iterator += 1
            readReversePolishString()
        }
        else {
            searchElse()
            iterator += 2
            readReversePolishString()
        }
        buffer.removeAt(0)
    }
    private fun runWhile() {
        var iteratorT: Int = iterator
        iterator += 1
        readReversePolishString()
        while (buffer[0].toBoolean()) {
            iterator += 1
            readReversePolishString()
            iterator = iteratorT
            readReversePolishString()
        }
        searchCloseBracket()
        iterator += 1
        readReversePolishString()
    }
    public fun readReversePolishString() {

        if (iterator < reversePolishString.size) {
            if (isNum(reversePolishString[iterator])) {
                buffer.add(0, reversePolishString[iterator])
                iterator += 1
                readReversePolishString()
            }
            else if (isVar(reversePolishString[iterator])) {
                buffer.add(0, reversePolishString[iterator])
                iterator += 1
                readReversePolishString()
            }
            else if (isArray(reversePolishString[iterator])) {
                buffer.add(0, reversePolishString[iterator])
                iterator += 1
                readReversePolishString()
            }
            else if (isAriphmSign(reversePolishString[iterator])) {
                if (isVar(buffer[0])) {
                    buffer[0] = program.vars[buffer[0]].toString()
                }
                if (isVar(buffer[1])) {
                    buffer[1] = program.vars[buffer[1]].toString()
                }
                buffer[0] = program.arithmetic(
                    buffer[0].toDouble(),
                    buffer[1].toDouble(),
                    reversePolishString[iterator]).toString()
                buffer.removeAt(1)
                iterator += 1
                readReversePolishString()
            }
            else if (isCompareSign(reversePolishString[iterator])) {
                if (isVar(buffer[0])) {
                    buffer[0] = program.vars[buffer[0]].toString()
                }
                if (isVar(buffer[1])) {
                    buffer[1] = program.vars[buffer[1]].toString()
                }
                buffer[0] = program.compare(
                    buffer[0].toDouble(),
                    buffer[1].toDouble(),
                    reversePolishString[iterator]).toString()
                buffer.removeAt(1)
                iterator += 1
                readReversePolishString()
            }
            else if (reversePolishString[iterator] == "init"){
                program.initializeVar(reversePolishString[iterator + 1])
                iterator += 2
                readReversePolishString()
            }
            else if (reversePolishString[iterator] == "initArray") {
                iterator += 1
                var array: MutableList<Double> = mutableListOf()
                var name: String = reversePolishString[iterator]
                iterator += 2
                while (isNum(reversePolishString[iterator])) {
                    array.add(array.size, reversePolishString[iterator].toDouble())
                }
                program.initializeArray(name, array)
                iterator += 1
                readReversePolishString()
            }
            else if (reversePolishString[iterator] == "assign") {
                program.assignVar(buffer[1], buffer[0].toDouble())
                buffer.removeAt(0)
                buffer.removeAt(0)
                iterator += 1
                readReversePolishString()
            }
            else if (reversePolishString[iterator] == "assignArray") {
                program.assignArray(buffer[2], buffer[1].toInt(), buffer[0].toDouble())
                buffer.removeAt(0)
                buffer.removeAt(0)
                buffer.removeAt(0)
                iterator += 1
                readReversePolishString()
            }
            else if (reversePolishString[iterator] == "if") {
                runIf()
                iterator += 1
                readReversePolishString()
            }
            else if (reversePolishString[iterator] == "while") {
                runWhile()
            }
            else if (reversePolishString[iterator] == "{" || reversePolishString[iterator] == "}") {
                return
            }
//            readReversePolishString()
        }
    }
}