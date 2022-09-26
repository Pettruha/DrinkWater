package com.iarigo.water.ui.fragment_weight

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.DateFormat
import java.util.*

class MyXAxisValueFormatter: IndexAxisValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        val timeMillis: Date = Date(value.toLong())
        val dateFormat: DateFormat = DateFormat.getDateInstance(DateFormat.DATE_FIELD, Locale.getDefault())
        return dateFormat.format(timeMillis)
    }
}