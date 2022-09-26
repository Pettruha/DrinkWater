package com.iarigo.water.ui.fragment_water

import android.content.Context
import com.github.mikephil.charting.data.BarEntry
import com.iarigo.water.storage.entity.Water

interface WaterContract {
    interface View {
        fun getFragmentContext(): Context
        fun setWaterLog(waterList: List<Water>)
        fun registryGraph(waterList: ArrayList<BarEntry>)
    }
    interface Presenter {
        fun viewIsReady(view: View)
        fun destroy()
        fun getWaters()
        fun getGraph()
    }
}