package com.example.mainver2

import android.widget.EditText
import android.widget.RelativeLayout

class VisualPrintBlock(private val layout: RelativeLayout) : VisualBlock {
    override fun toDslString(): String {
        val value = layout.findViewWithTag<EditText>("print_value")?.text?.toString()?.trim().orEmpty()
        return if (value.isNotEmpty()) "print $value" else ""
    }
}
