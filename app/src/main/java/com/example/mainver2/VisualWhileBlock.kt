package com.example.mainver2

import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout

class VisualWhileBlock(private val layout: RelativeLayout) : VisualBlock {
    override fun toDslString(): String {
        val conditionContainer = layout.getChildAt(0) as? LinearLayout
        val actionContainer = layout.getChildAt(1) as? LinearLayout

        val condition = conditionContainer?.findViewWithTag<EditText>("while_condition")?.text?.toString()?.trim().orEmpty()
        val action = actionContainer?.findViewWithTag<EditText>("while_action")?.text?.toString()?.trim().orEmpty()

        return if (condition.isNotEmpty())
            "while ($condition) { $action }"
        else
            ""
    }
}
