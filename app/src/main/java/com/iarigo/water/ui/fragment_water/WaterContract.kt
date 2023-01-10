package com.iarigo.water.ui.fragment_water

import android.app.Application
import android.content.Context
import com.github.mikephil.charting.data.BarEntry
import com.iarigo.water.storage.entity.Water

interface WaterContract {
    interface View {
        fun getFragmentContext(): Context
        fun getApplication(): Application
        fun setWaterLog(waterList: List<Water>)
        fun showGraph(waterList: ArrayList<BarEntry>, datesList: ArrayList<String>)
        fun setWaterDay(day: String)
        fun showAddWater(show: Boolean)
    }
    interface Presenter {
        fun viewIsReady(view: View)
        fun destroy()
        fun getWaters()
        fun getGraph()
        fun addDrink()
        fun setCurrentDay()
        fun setNewCurrentDay(day: String)
    }
}