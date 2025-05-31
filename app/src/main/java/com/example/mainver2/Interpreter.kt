package com.example.mainver2

import android.util.Log
import android.widget.TextView

class Interpreter {
    var iterator: Int = 0
    var program: Program
    var reversePolishString: MutableList<String>
    var buffer: MutableList<String> = mutableListOf()
    var console: TextView

    constructor(reversePolishString: MutableList<String>, console: TextView) {
        this.reversePolishString = reversePolishString
        this.program = Program()
        this.console = console
    }
//    private fun checkErrors() {
//        if ()
//    }
    private fun error(message: String) {
        console.text = message
        iterator = reversePolishString.size
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
    }
    public fun readReversePolishString() {

        if (iterator < reversePolishString.size) {
            Log.d("Interpreter", iterator.toString())
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
                    if (buffer.size > 1 && isArray(buffer[1])) {
                        buffer[0] = program.arrays[buffer[1]]!![buffer[0].toInt()].toString()
                        buffer.removeAt(1)
                    }
                    else {
                        buffer[0] = program.vars[buffer[0]].toString()
                    }
                }
                if (isVar(buffer[1])) {
                    if (buffer.size > 2 && isArray(buffer[2])) {
                        buffer[1] = program.arrays[buffer[2]]!![buffer[1].toInt()].toString()
                        buffer.removeAt(2)
                    }
                    else {
                        buffer[1] = program.vars[buffer[1]].toString()
                    }
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
                    if (buffer.size > 1 && isArray(buffer[1])) {
                        buffer[0] = program.arrays[buffer[1]]!![buffer[0].toInt()].toString()
                        buffer.removeAt(1)
                    }
                    else {
                        buffer[0] = program.vars[buffer[0]].toString()
                    }
                }
                if (isVar(buffer[1])) {
                    if (buffer.size > 2 && isArray(buffer[2])) {
                        buffer[1] = program.arrays[buffer[2]]!![buffer[1].toInt()].toString()
                        buffer.removeAt(2)
                    }
                    else {
                        buffer[1] = program.vars[buffer[1]].toString()
                    }
                }
                buffer[0] = program.compare(
                    buffer[0].toDouble(),
                    buffer[1].toDouble(),
                    reversePolishString[iterator]).toString()
                buffer.removeAt(1)
                iterator += 1
                readReversePolishString()
            }
            else if (reversePolishString[iterator] == "||") {
                buffer[0] = (buffer[0].toBoolean() || buffer[1].toBoolean()).toString()
                buffer.removeAt(1)
            }
            else if (reversePolishString[iterator] == "&&") {
                buffer[0] = (buffer[0].toBoolean() && buffer[1].toBoolean()).toString()
                buffer.removeAt(1)
            }
            else if (reversePolishString[iterator] == "init"){
                program.initializeVar(reversePolishString[iterator + 1])
                iterator += 2
                readReversePolishString()
            }
            else if (reversePolishString[iterator] == "intArray") {
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
                if (isVar(buffer[0])) {
                    if (buffer.size > 1) {
                        buffer[0] = program.arrays[buffer[1]]!![buffer[0].toInt()].toString()
                        buffer.removeAt(1)
                    }
                    else {
                        buffer[0] = program.vars[buffer[0]].toString()
                    }
                }
                program.assignVar(buffer[1], buffer[0].toDouble())
                buffer.removeAt(0)
                buffer.removeAt(0)
                iterator += 1
                readReversePolishString()
            }
            else if (reversePolishString[iterator] == "assignArray") {
                if (isVar(buffer[0])) {
                    if (buffer.size > 1 && isArray(buffer[1])) {
                        buffer[0] = program.arrays[buffer[1]]!![buffer[0].toInt()].toString()
                        buffer.removeAt(1)
                    }
                    else {
                        buffer[0] = program.vars[buffer[0]].toString()
                    }
                }
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
                iterator += 1
                readReversePolishString()
            }
            else if (reversePolishString[iterator] == "{" || reversePolishString[iterator] == "}") {
                return
            }
//            else {
//                error("undefind name" + reversePolishString[iterator])
//            }
//            readReversePolishString()
            Log.d("Interpreter", buffer.toString())
        }
    }
}