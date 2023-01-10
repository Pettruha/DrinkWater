package com.iarigo.water.ui.dialogGender

import android.app.Application
import android.content.Context
import com.iarigo.water.storage.entity.User

interface GenderContract {
    interface View {
        fun getApplication(): Application
        fun getDialogContext(): Context
        fun closeDialog()
        fun setGender(user: User)
    }
    interface Presenter {
        fun viewIsReady(view: View)
        fun destroy()
        fun getGender()
        fun saveGender(user: User)
    }
}