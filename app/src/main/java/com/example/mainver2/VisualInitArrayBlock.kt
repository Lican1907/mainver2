package com.example.mainver2

import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout

class VisualInitArrayBlock(private val layout: RelativeLayout) : VisualBlock {
    override fun toDslString(): String {
        val row1 = layout.getChildAt(0) as? LinearLayout
        val row2 = layout.getChildAt(1) as? LinearLayout
        val row3 = layout.getChildAt(2) as? LinearLayout

        val arrayName = row1?.findViewWithTag<EditText>("array_name")?.text?.toString()?.trim().orEmpty()
        val arraySize = row2?.findViewWithTag<EditText>("array_size")?.text?.toString()?.trim().orEmpty()
        val arrayData = row3?.findViewWithTag<EditText>("array_data")?.text?.toString()?.trim().orEmpty()

        return if (arrayName.isNotEmpty()) {
            when {
                arrayData.isNotEmpty() -> "intArray $arrayName = { $arrayData }"
                arraySize.isNotEmpty() -> "intArray $arrayName $arraySize"
                else -> "intArray $arrayName"
            }
        } else {
            ""
        }
    }
}
