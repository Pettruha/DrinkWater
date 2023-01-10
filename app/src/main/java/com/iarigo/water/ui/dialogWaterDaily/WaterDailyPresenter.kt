package com.iarigo.water.ui.dialogWaterDaily

import com.iarigo.water.repository.PreferencesRepository
import com.iarigo.water.storage.database.AppDatabase
import io.reactivex.disposables.CompositeDisposable

class WaterDailyPresenter: WaterDailyContract.Presenter {
    private lateinit var dialogView: WaterDailyContract.View
    private var appDatabase: AppDatabase? = null // Database
    private val subscriptions = CompositeDisposable()
    private lateinit var preferences: PreferencesRepository

    override fun viewIsReady(view: WaterDailyContract.View) {
        dialogView = view
        appDatabase = AppDatabase.getAppDataBase(dialogView.getDialogContext())
        preferences = PreferencesRepository(dialogView.getApplication())
    }

    override fun destroy() {
        subscriptions.dispose()
    }

    override fun getCurrentWater() {
        val water = preferences.getDrinkCountPerDay()
        dialogView.setCurrentWater(water.toString())
    }

    override fun saveWater(water: String) {
        if (water != "") {
            val waterInt: Int = water.toInt()
            if (waterInt > 0) {
                preferences.saveDrinkCountPerDay(waterInt)
                dialogView.closeDialog()
            } else {
                dialogView.showWaterError()
            }
        } else {
            dialogView.showWaterError()
        }
    }
}