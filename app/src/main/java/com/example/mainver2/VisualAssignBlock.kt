package com.example.mainver2

import android.widget.EditText
import android.widget.RelativeLayout

class VisualAssignBlock(private val layout: RelativeLayout) : VisualBlock {
    override fun toDslString(): String {
        val varName = layout.findViewWithTag<EditText>("assign_input_first")?.text?.toString()?.trim().orEmpty()
        val value = layout.findViewWithTag<EditText>("assign_input_second")?.text?.toString()?.trim().orEmpty()

        return if (varName.isNotEmpty() && value.isNotEmpty())
            "assign $varName = $value;"
        else
            ""
    }
}
