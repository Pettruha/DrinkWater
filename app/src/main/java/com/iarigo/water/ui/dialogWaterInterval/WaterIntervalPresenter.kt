package com.iarigo.water.ui.dialogWaterInterval

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import com.iarigo.water.helper.Helper
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.User
import com.iarigo.water.ui.dialogWaterPeriod.WaterPeriodContract
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat

class WaterIntervalPresenter: WaterIntervalContract.Presenter {
    private lateinit var dialogView: WaterIntervalContract.View
    private var appDatabase: AppDatabase? = null // БД
    private var disposable: Disposable? = null // вызов subscribe
    private val subscriptions = CompositeDisposable()
    private lateinit var mSettings: SharedPreferences

    override fun viewIsReady(view: WaterIntervalContract.View) {
        dialogView = view
        appDatabase = AppDatabase.getAppDataBase(dialogView.getDialogContext())
        mSettings = dialogView.getDialogContext().getSharedPreferences("water", Context.MODE_PRIVATE)
        getInterval()
    }

    private fun getInterval() {
        val hour = mSettings.getInt(Helper.NOTIFY_FREQ_HOUR, 1)
        val minute = mSettings.getInt(Helper.NOTIFY_FREQ_MINUTE, 0)
        dialogView.setHour(hour)
        dialogView.setMinute(minute)
    }

    override fun save(selectedHour: String, selectedMinute: String) {
        var errors : Boolean = false

        // интервал более 15 минут
        if (selectedHour == "0") {
            if (selectedMinute < "15") {
                errors = true
                dialogView.showTimeError()
            }
        }

        if (!errors) {
            // сохраняем период

            val e = mSettings.edit()
            e.putInt(Helper.NOTIFY_FREQ_HOUR, selectedHour.toInt())
            e.putInt(Helper.NOTIFY_FREQ_MINUTE, selectedMinute.toInt())
            e.apply()

            dialogView.closeDialog()
        }
    }

    override fun destroy() {
        subscriptions.dispose() // очищаем потоки
        disposable?.dispose()
    }
}