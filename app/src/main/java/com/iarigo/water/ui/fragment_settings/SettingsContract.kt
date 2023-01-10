package com.iarigo.water.ui.fragment_settings

import android.app.Application
import android.content.Context

interface SettingsContract {
    interface View {
        fun getFragmentContext(): Context
        fun getApplication(): Application
        fun hideRate()
        fun setGender(gender: Int)
        fun setWeight(weight: Double)
        fun setWaterCount(waterCount: String)
        fun setWaterPersonal(personal: Boolean)
    }
    interface Presenter {
        fun viewIsReady(view: View)
        fun destroy()
        fun saveWaterCountPersonal(personal: Boolean)
        fun fillValues()
        fun getWaterDaily()
        fun getGender()
        fun getWeight()
        fun saveRate()
    }
}