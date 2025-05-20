package com.example.mainver2

class Interpreter {
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
    private fun isAriphmSign(el: String): Boolean {
        return el in setOf("+", "-", "/", "*", "%")
    }
    private fun isCompareSign(el: String): Boolean {
        return el in setOf(">", "<", "<=", ">=", "==", "!=")
    }
    private fun runIf(iTrue: Int, iFalse: Int, iCur: Int): Int {
        if (buffer[2].toBoolean()) {
            buffer.removeAt(0)
            buffer.removeAt(1)
            buffer.removeAt(2)
            return iTrue
        }
        else {
            buffer.removeAt(0)
            buffer.removeAt(1)
            buffer.removeAt(2)
            return iFalse
        }
    }
    public fun main() {
        var iterator: Int = 0
        while (iterator < reversePolishString.size) {
            var elementOfStack = reversePolishString[iterator]
            if (isNum(elementOfStack)) {
                buffer.add(0, elementOfStack)
                iterator += 1
            }
            else if (isVar(elementOfStack)) {
                buffer.add(0, elementOfStack)
                iterator += 1
            }
            else if (isAriphmSign(elementOfStack)) {
                if (isVar(buffer[0])) {
                    buffer[0] = program.vars[buffer[0]].toString()
                }
                if (isVar(buffer[1])) {
                    buffer[1] = program.vars[buffer[1]].toString()
                }
                buffer[0] = program.arithmetic(
                    buffer[0].toDouble(),
                    buffer[1].toDouble(),
                    elementOfStack).toString()
                buffer.removeAt(1)
            }
            else if (isCompareSign(elementOfStack)) {
                if (isVar(buffer[0])) {
                    buffer[0] = program.vars[buffer[0]].toString()
                }
                if (isVar(buffer[1])) {
                    buffer[1] = program.vars[buffer[1]].toString()
                }
                buffer[0] = program.compare(
                    buffer[0].toDouble(),
                    buffer[1].toDouble(),
                    elementOfStack).toString()
                buffer.removeAt(1)
            }
            else if (elementOfStack == "init"){
                program.initializeVar(buffer[0])
                buffer.removeAt(0)
            }
            else if (elementOfStack == "assign") {
                program.assignVar(buffer[1], buffer[0].toDouble())
                buffer.removeAt(0)
                buffer.removeAt(1)
            }
            else if (elementOfStack == "GoTo") {
                iterator = runIf(buffer[1].toInt(), buffer[0].toInt(), iterator)
            }
        }
    }
}