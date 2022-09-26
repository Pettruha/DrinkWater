package com.iarigo.water.ui.dialogWaterDaily

import android.content.Context
import com.iarigo.water.storage.entity.Water
import com.iarigo.water.storage.entity.Weight

interface WaterDailyContract {
    interface View {
        fun getDialogContext(): Context
        fun closeDialog() // Закрываем диалоговое окно
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