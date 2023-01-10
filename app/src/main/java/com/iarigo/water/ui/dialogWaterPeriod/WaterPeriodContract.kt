package com.iarigo.water.ui.dialogWaterPeriod

import android.app.Application
import android.content.Context
import android.os.Bundle
import com.iarigo.water.storage.entity.User

interface WaterPeriodContract {
    interface View {
        fun getApplication(): Application
        fun getDialogContext(): Context
        fun closeDialog(bundle: Bundle)
        fun showTimeError(error: Int)
        fun setPeriod(user: User) // set up user period
    }
    interface Presenter {
        fun viewIsReady(view: View)
        fun destroy()
        fun save(user: User)
    }
}