package com.iarigo.water.ui.dialogDrink

import android.content.Context

interface DrinkContract {
    interface View {
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