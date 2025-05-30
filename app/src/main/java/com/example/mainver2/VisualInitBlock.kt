package com.example.mainver2

import android.widget.EditText
import android.widget.RelativeLayout

class VisualInitBlock(private val layout: RelativeLayout) : VisualBlock {
    override fun toDslString(): String {
        val variable = layout.findViewWithTag<EditText>("init_variable")?.text?.toString()?.trim().orEmpty()
        val value = layout.findViewWithTag<EditText>("init_value")?.text?.toString()?.trim().orEmpty()

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
