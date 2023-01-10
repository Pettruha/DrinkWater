package com.iarigo.water.ui.dialogDrinkSelect

import android.annotation.SuppressLint
import com.iarigo.water.repository.PreferencesRepository
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.Drinks
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class DrinkPresenter: DrinkContract.Presenter {
    private lateinit var dialogView: DrinkContract.View
    private var appDatabase: AppDatabase? = null // Database
    private val subscriptions = CompositeDisposable()
    private lateinit var preferences: PreferencesRepository

    override fun viewIsReady(view: DrinkContract.View) {
        dialogView = view
        appDatabase = AppDatabase.getAppDataBase(dialogView.getDialogContext())
        preferences = PreferencesRepository(dialogView.getApplication())
    }

    override fun destroy() {
        subscriptions.dispose()
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

    @SuppressLint("DiscouragedApi")
    private fun createDrinksList(drinks: List<Drinks>) {

        val aList: ArrayList<HashMap<String, String>> = ArrayList()
        val selectedDrink = preferences.getDrinkType()

        for (drink in drinks) {
            val hm: HashMap<String, String> = HashMap()
            hm["Id"] = drink.id.toString() // list id
            if (drink.system)
                hm["name"] = dialogView.getDialogContext().getString(dialogView.getDialogContext().resources.getIdentifier(drink.name, "string", dialogView.getDialogContext().packageName))
            else hm["name"] = drink.name
            hm["percent"] = drink.percent.toString()
            hm["selected"] = if(selectedDrink == drink.id) "1" else "0"

            aList.add(hm)
        }

        dialogView.updateDrinkList(aList)
    }

    override fun saveDrink(id: String) {
        preferences.saveDrinkType(id.toLong())
        getDrinks()
        dialogView.drinkSelected()
    }
}