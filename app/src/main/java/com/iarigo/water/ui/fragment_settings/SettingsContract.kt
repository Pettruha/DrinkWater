package com.iarigo.water.ui.fragment_settings

import android.content.Context

interface SettingsContract {
    interface View {
        fun getFragmentContext(): Context
        fun hideRate()
        fun setSex(sex: Int)
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
        fun getSex()
        fun getWeight()
    }
}