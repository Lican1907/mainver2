package com.example.mainver2

import android.widget.EditText
import android.widget.RelativeLayout

// Пример реализации VisualBlock для блока init
class VisualInitBlock(private val layout: RelativeLayout) : VisualBlock {
    override fun toDslString(): String {
        // Предполагается, что EditText для имени переменной имеет тег "init_variable",
        // а для значения — тег "init_value"
        val variable = layout.findViewWithTag<EditText>("init_variable")?.text?.toString()?.trim().orEmpty()
        val value = layout.findViewWithTag<EditText>("init_value")?.text?.toString()?.trim().orEmpty()

        // Формируем DSL-строку. Если значение не пустое, добавляем команду assign.
        return if (variable.isNotEmpty()) {
            if (value.isNotEmpty())
                "init $variable;\nassign $variable = $value;"
            else
                "init $variable;"
        } else {
            ""
        }
    }
}
