package com.iarigo.water.ui.dialogWaterInterval

import com.iarigo.water.repository.PreferencesRepository
import com.iarigo.water.storage.database.AppDatabase
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class WaterIntervalPresenter: WaterIntervalContract.Presenter {
    private lateinit var dialogView: WaterIntervalContract.View
    private var appDatabase: AppDatabase? = null // Database
    private var disposable: Disposable? = null
    private val subscriptions = CompositeDisposable()
    private lateinit var preferences: PreferencesRepository

    override fun viewIsReady(view: WaterIntervalContract.View) {
        dialogView = view
        appDatabase = AppDatabase.getAppDataBase(dialogView.getDialogContext())
        preferences = PreferencesRepository(dialogView.getApplication())
        getInterval()
    }

    private fun getInterval() {
        dialogView.setHour(preferences.getWaterIntervalHour())
        dialogView.setMinute(preferences.getWaterIntervalMinute())
    }

    override fun save(selectedHour: String, selectedMinute: String) {
        var errors : Boolean = false

        // interval must be more 15 minutes
        if (selectedHour == "0") {
            if (selectedMinute < "15") {
                errors = true
                dialogView.showTimeError()
            }
        }

        if (!errors) {
            // save period
            preferences.saveWaterIntervalHour(selectedHour.toInt())
            preferences.saveWaterIntervalMinute(selectedMinute.toInt())

            dialogView.closeDialog()
        }
    }

    override fun destroy() {
        subscriptions.dispose()
    }
}