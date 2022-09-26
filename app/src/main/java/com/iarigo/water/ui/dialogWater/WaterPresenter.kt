package com.iarigo.water.ui.dialogWater

import android.content.Context
import android.content.SharedPreferences
import com.iarigo.water.helper.Helper
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.ui.dialogSex.SexContract
import io.reactivex.disposables.CompositeDisposable

class WaterPresenter: WaterContract.Presenter {
    private lateinit var dialogView: WaterContract.View
    private var appDatabase: AppDatabase? = null // БД
    private val subscriptions = CompositeDisposable()
    private lateinit var mSettings: SharedPreferences

    override fun viewIsReady(view: WaterContract.View) {
        dialogView = view
        appDatabase = AppDatabase.getAppDataBase(dialogView.getDialogContext())
        mSettings = dialogView.getDialogContext().getSharedPreferences("water", Context.MODE_PRIVATE)
    }

    override fun destroy() {
        subscriptions.dispose() // очищаем потоки
    }

    override fun getCurrentWeight() {
        dialogView.setCurrentWater(mSettings.getString(Helper.WATER_COUNT, "200")!!)
    }

    override fun saveWater(water: String) {
        val waterInt: Int = water.toInt()
        if (waterInt >= 10) {
            val e = mSettings.edit()
            e.putString(Helper.WATER_COUNT, waterInt.toString())
            e.apply()

            dialogView.closeDialog()
        } else {
            dialogView.showWaterError()
        }
    }

}