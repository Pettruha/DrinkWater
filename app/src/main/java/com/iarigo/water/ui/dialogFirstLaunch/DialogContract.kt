package com.iarigo.water.ui.dialogFirstLaunch

import android.content.Context
import android.os.Bundle
import android.text.Editable

interface DialogContract {
    interface View {
        fun getDialogContext(): Context
        fun closeDialog(bundle: Bundle) // Закрываем диалоговое окно
        fun setWaterCount(water: Double) // устанавливаем норму воды в сутки
        fun showWeightError() // ошибка веса
        fun showSexError() // ошибка пола
        fun showTimeError(error: Int) // ошибка времени
    }
    interface Presenter {
        fun viewIsReady(view: DialogContract.View)
        fun saveUser(bundle: Bundle) // сохранить
        fun destroy()
        fun calculateWater(editable: Editable?) // пересчитываем кол-во воды в сутки
    }
}