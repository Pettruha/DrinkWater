package com.iarigo.water.ui.fragment_main

import android.content.Context

interface MainContract {
    interface View {
        fun getFragmentContext(): Context
        fun setWaterCount(water: Int, waterTotal: Int)
        fun setCat(type: Int)
        fun setWaterPerOnce(count: Int)
    }
    interface Presenter {
        fun viewIsReady(view: View)
        fun destroy()
        fun addWater()
        fun setWaterCountPerOnce()
    }
}