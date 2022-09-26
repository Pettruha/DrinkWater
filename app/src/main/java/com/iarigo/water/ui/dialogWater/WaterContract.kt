package com.iarigo.water.ui.dialogWater

import android.content.Context
import com.iarigo.water.storage.entity.Weight

interface WaterContract {
    interface View {
        fun getDialogContext(): Context
        fun closeDialog() // Закрываем диалоговое окно
        fun setCurrentWater(water: String)
        fun showWaterError()
    }
    interface Presenter {
        fun viewIsReady(view: View)
        fun destroy()
        fun getCurrentWeight()
        fun saveWater(water: String)
    }
}