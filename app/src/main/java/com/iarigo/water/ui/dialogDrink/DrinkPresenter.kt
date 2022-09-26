package com.iarigo.water.ui.dialogDrink

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.iarigo.water.helper.Helper
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.Drinks
import com.iarigo.water.storage.entity.User
import com.iarigo.water.ui.dialogSex.SexContract
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class DrinkPresenter: DrinkContract.Presenter {
    private lateinit var dialogView: DrinkContract.View
    private var appDatabase: AppDatabase? = null // БД
    private val subscriptions = CompositeDisposable()
    private lateinit var mSettings: SharedPreferences

    override fun viewIsReady(view: DrinkContract.View) {
        dialogView = view
        appDatabase = AppDatabase.getAppDataBase(dialogView.getDialogContext())
        mSettings = dialogView.getDialogContext().getSharedPreferences("water", Context.MODE_PRIVATE)
    }

    override fun destroy() {
        subscriptions.dispose() // очищаем потоки
    }

    override fun getDrinks() {
        val subscribe = appDatabase?.drinksDao()?.getDrinks()?.subscribeOn(
            Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ drinks: List<Drinks> ->
                createDrinksList(drinks)
            }, { error ->
                error.printStackTrace()
            })
        subscriptions.add(subscribe!!)
    }

    private fun createDrinksList(drinks: List<Drinks>) {

        val aList: ArrayList<HashMap<String, String>> = ArrayList()
        val selectedDrink = mSettings.getLong(Helper.WATER_DRINK_SELECTED, 1L)

        for (drink in drinks) {
            val hm: HashMap<String, String> = HashMap()
            hm["Id"] = drink.id.toString() // id списка
            hm["name"] = drink.name
            hm["percent"] = drink.percent.toString()
            hm["selected"] = if(selectedDrink == drink.id) "1" else "0"

            aList.add(hm)
        }

        dialogView.updateDrinkList(aList)
    }

    override fun saveDrink(id: String) {
        val e = mSettings.edit()
        e.putLong(Helper.WATER_DRINK_SELECTED, id.toLong())
        e.apply()
        getDrinks()
        dialogView.drinkSelected()
    }
}