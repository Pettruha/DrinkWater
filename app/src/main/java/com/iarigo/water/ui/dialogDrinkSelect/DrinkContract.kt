package com.iarigo.water.ui.dialogDrinkSelect

import android.app.Application
import android.content.Context

interface DrinkContract {
    interface View {
        fun getApplication(): Application
        fun getDialogContext(): Context
        fun updateDrinkList(list: ArrayList<HashMap<String, String>>)
        fun drinkSelected()
    }
    interface Presenter {
        fun viewIsReady(view: View)
        fun destroy()
        fun getDrinks()
        fun saveDrink(id: String)
    }
}