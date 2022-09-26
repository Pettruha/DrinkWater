package com.iarigo.water.ui.dialogWaterPeriod

import android.content.Context
import android.os.Bundle
import android.text.Editable
import com.iarigo.water.storage.entity.User
import com.iarigo.water.ui.dialogFirstLaunch.DialogContract

interface WaterPeriodContract {
    interface View {
        fun getDialogContext(): Context
        fun closeDialog(bundle: Bundle) // Закрываем диалоговое окно
        fun showTimeError(error: Int) // ошибка времени
        fun setPeriod(user: User) // устанавливаем начальное время
    }
    interface Presenter {
        fun viewIsReady(view: View)
        fun destroy()
        fun save(user: User)
    }
}