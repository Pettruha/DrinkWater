package com.iarigo.water.ui.fragment_water

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.util.*

/**
 * Date format without year
 */
class MyXAxisShortValueFormatter(datesList: ArrayList<String>): IndexAxisValueFormatter() {

    private val dates = datesList

    override fun getFormattedValue(value: Float): String {
        return dates[value.toInt()]
    }
}