package com.iarigo.water.ui.dialogWeight

import android.content.Context
import android.os.Bundle
import com.iarigo.water.storage.entity.Weight

interface WeightContract {
    interface View {
        fun getDialogContext(): Context
        fun closeDialog() // Закрываем диалоговое окно
        fun setCurrentWeight(weight: Weight)
        fun showWeightError()
    }
    interface Presenter {
        fun viewIsReady(view: View)
        fun destroy()
        fun getCurrentWeight()
        fun saveWeight(weight: String)
    }
}