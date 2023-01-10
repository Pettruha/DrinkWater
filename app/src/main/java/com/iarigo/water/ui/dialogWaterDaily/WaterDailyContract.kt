package com.iarigo.water.ui.dialogWaterDaily

import android.app.Application
import android.content.Context
import com.iarigo.water.storage.entity.Water
import com.iarigo.water.storage.entity.Weight

interface WaterDailyContract {
    interface View {
        fun getApplication(): Application
        fun getDialogContext(): Context
        fun closeDialog()
        fun setCurrentWater(waterCount: String)
        fun showWaterError()
    }
    interface Presenter {
        fun viewIsReady(view: View)
        fun destroy()
        fun getCurrentWater()
        fun saveWater(water: String)
    }
}