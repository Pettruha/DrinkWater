package com.iarigo.water.ui.dialogWaterInterval

import android.app.Application
import android.content.Context
import android.os.Bundle
import com.iarigo.water.storage.entity.User

interface WaterIntervalContract {
    interface View {
        fun getApplication(): Application
        fun getDialogContext(): Context
        fun closeDialog()
        fun showTimeError()
        fun setHour(hour: Int)
        fun setMinute(minute: Int)
    }
    interface Presenter {
        fun viewIsReady(view: View)
        fun destroy()
        fun save(hour: String, minute: String)
    }
}