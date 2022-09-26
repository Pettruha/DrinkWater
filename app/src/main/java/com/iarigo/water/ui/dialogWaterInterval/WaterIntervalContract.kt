package com.iarigo.water.ui.dialogWaterInterval

import android.content.Context
import android.os.Bundle
import com.iarigo.water.storage.entity.User

interface WaterIntervalContract {
    interface View {
        fun getDialogContext(): Context
        fun closeDialog() // Закрываем диалоговое окно
        fun showTimeError() // ошибка периода
        fun setHour(hour: Int)
        fun setMinute(minute: Int)
    }
    interface Presenter {
        fun viewIsReady(view: View)
        fun destroy()
        fun save(hour: String, minute: String)
    }
}