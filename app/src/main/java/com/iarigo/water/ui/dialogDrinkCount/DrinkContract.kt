package com.iarigo.water.ui.dialogDrinkCount

import android.app.Application
import android.content.Context

interface DrinkContract {
    interface View {
        fun getApplication(): Application
        fun getDialogContext(): Context
        fun closeDialog(water: Int)
        fun setCurrentWater(water: Int)
        fun showWaterError()
    }
    interface Presenter {
        fun viewIsReady(view: View)
        fun destroy()
        fun getCurrentDrinkCount()
        fun saveWater(water: String)
    }
}