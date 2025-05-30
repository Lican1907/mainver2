package com.example.mainver2

import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout

class VisualIfElseBlock(private val layout: RelativeLayout) : VisualBlock {
    override fun toDslString(): String {
        // Извлечение контейнеров строк (если структура фиксирована)
        val conditionContainer = layout.getChildAt(0) as? LinearLayout
        val thenContainer = layout.getChildAt(1) as? LinearLayout
        val elseContainer = layout.getChildAt(2) as? LinearLayout

        val condition = conditionContainer?.findViewWithTag<EditText>("if_condition")?.text?.toString()?.trim().orEmpty()
        val thenText = thenContainer?.findViewWithTag<EditText>("if_then")?.text?.toString()?.trim().orEmpty()
        val elseText = elseContainer?.findViewWithTag<EditText>("if_else")?.text?.toString()?.trim().orEmpty()

        return if (condition.isNotEmpty())
            "if ($condition) { $thenText } else { $elseText }"
        else
            ""
    }
}
