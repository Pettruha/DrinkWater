package com.iarigo.water.ui.fragment_notify

import android.app.Application
import android.content.Context

interface NotifyContract {
    interface View {
        fun getFragmentContext(): Context
        fun getApplication(): Application
        fun setPeriod(start: String, end: String)
        fun setNormaOver(over: Boolean)
        fun setFreq(string: String)
        fun setNotifyOn(on: Boolean)
    }
    interface Presenter {
        fun viewIsReady(view: View)
        fun destroy()
        fun getParams()
        fun getSound(): String
        fun getFreq()

        fun saveOver(over: Boolean)
        fun saveNotifyOn(on: Boolean)
        fun saveWaterPeriod(wakeUpHour: Int, wakeUpMinute: Int, goBedHour: Int, goBedMinute: Int)
    }
}