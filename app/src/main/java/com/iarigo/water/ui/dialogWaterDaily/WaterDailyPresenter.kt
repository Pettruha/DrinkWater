package com.iarigo.water.ui.dialogWaterDaily

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.widget.ThemedSpinnerAdapter
import com.iarigo.water.helper.Helper
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.ui.dialogWater.WaterContract
import io.reactivex.disposables.CompositeDisposable

class WaterDailyPresenter: WaterDailyContract.Presenter {
    private lateinit var dialogView: WaterDailyContract.View
    private var appDatabase: AppDatabase? = null // БД
    private val subscriptions = CompositeDisposable()
    private lateinit var mSettings: SharedPreferences

    override fun viewIsReady(view: WaterDailyContract.View) {
        dialogView = view
        appDatabase = AppDatabase.getAppDataBase(dialogView.getDialogContext())
        mSettings = dialogView.getDialogContext().getSharedPreferences("water", Context.MODE_PRIVATE)
    }

    override fun destroy() {
        subscriptions.dispose() // очищаем потоки
    }

    override fun getCurrentWater() {
        val water = mSettings.getString(Helper.WATER_COUNT_PER_DAY, "1800")
        if (water != null)
            dialogView.setCurrentWater(water)
        else
            dialogView.setCurrentWater("1800")
    }

    override fun saveWater(water: String) {
        if (water != "") {
            if (water.toInt() > 0) {
                val e = mSettings.edit()
                e.putString(Helper.WATER_COUNT_PER_DAY, water)
                e.apply()
                dialogView.closeDialog()
            } else {
                dialogView.showWaterError()
            }
        } else {
            dialogView.showWaterError()
        }
    }
}