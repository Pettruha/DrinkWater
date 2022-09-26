package com.iarigo.water.ui.dialogSex

import android.content.Context
import com.iarigo.water.storage.entity.User
import com.iarigo.water.storage.entity.Weight

interface SexContract {
    interface View {
        fun getDialogContext(): Context
        fun closeDialog() // Закрываем диалоговое окно
        fun setSex(user: User)
    }
    interface Presenter {
        fun viewIsReady(view: View)
        fun destroy()
        fun getSex()
        fun saveSex(user: User)
    }
}