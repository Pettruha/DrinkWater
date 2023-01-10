package com.iarigo.water.ui.dialogDrinkCount

import com.iarigo.water.repository.PreferencesRepository
import com.iarigo.water.storage.database.AppDatabase
import io.reactivex.disposables.CompositeDisposable

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

    override fun getCurrentDrinkCount() {
        dialogView.setCurrentWater(preferences.getDrinkCount())
    }

    override fun saveWater(water: String) {
        val waterInt: Int = water.toInt()
        if (waterInt >= 10) {
            preferences.saveDrinkCount(waterInt)

            dialogView.closeDialog(waterInt)
        } else {
            dialogView.showWaterError()
        }
    }

}