package com.iarigo.water.ui.fragment_water

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MyXAxisShortValueFormatter: IndexAxisValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        val timeMillis: Date = Date(TimeUnit.DAYS.toMillis(value.toLong()))
        val oldFormatter = SimpleDateFormat("dd.MM", Locale.getDefault())
        return oldFormatter.format(timeMillis)
    }
}