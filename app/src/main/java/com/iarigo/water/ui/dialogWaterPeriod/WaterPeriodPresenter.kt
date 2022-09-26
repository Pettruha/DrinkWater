package com.iarigo.water.ui.dialogWaterPeriod

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import com.iarigo.water.helper.Helper
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.User
import com.iarigo.water.storage.entity.Weight
import com.iarigo.water.ui.dialogFirstLaunch.DialogContract
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

class WaterPeriodPresenter: WaterPeriodContract.Presenter {
    private lateinit var dialogView: WaterPeriodContract.View
    private var appDatabase: AppDatabase? = null // БД
    private var disposable: Disposable? = null // вызов subscribe
    private val subscriptions = CompositeDisposable()
    private lateinit var mSettings: SharedPreferences

    override fun viewIsReady(view: WaterPeriodContract.View) {
        dialogView = view
        appDatabase = AppDatabase.getAppDataBase(dialogView.getDialogContext())
        mSettings = dialogView.getDialogContext().getSharedPreferences("water", Context.MODE_PRIVATE)
        getPeriod()
    }

    private fun getPeriod() {
        val subscribe = appDatabase?.userDao()?.getUser(mSettings.getLong(Helper.USER_ID, 1L))?.subscribeOn(
            Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ user: User ->
                dialogView.setPeriod(user)

            }, { error ->
                error.printStackTrace()
            })
        subscriptions.add(subscribe!!)
    }

    @SuppressLint("SimpleDateFormat")
    override fun save(user: User) {
        var errors : Boolean = false
        // время пробуждения - время сна более 3-х часов
        val simpleDateFormat = SimpleDateFormat("hh:mm")

        val date1 = simpleDateFormat.parse("${user.wakeUpHour}:${user.wakeUpMinute}")
        val date2 = simpleDateFormat.parse("${user.bedHour}:${user.bedMinute}")

        val difference: Long = date2.time - date1.time
        val days = (difference / (1000 * 60 * 60 * 24)).toInt()
        val hours = ((difference - 1000 * 60 * 60 * 24 * days) / (1000 * 60 * 60)).toInt()
        // hours = if (hours < 0) -hours else hours
        Log.i("======= Hours", " :: $hours")

        if (hours < 0) {
            errors = true
            dialogView.showTimeError(0)
        } else if (hours < 3) {
            errors = true
            dialogView.showTimeError(1)
        }

        if (!errors) {
            // сохраняем поль-ля
            appDatabase?.userDao()?.update(user)

            val bundle: Bundle = Bundle()
            bundle.putInt("wakeup_time_hour", user.wakeUpHour)
            bundle.putInt("wakeup_time_minute", user.wakeUpMinute)
            bundle.putInt("go_bed_time_hour", user.bedHour)
            bundle.putInt("go_bed_time_minute", user.bedMinute)

            dialogView.closeDialog(bundle)
        }
    }

    override fun destroy() {
        subscriptions.dispose() // очищаем потоки
        disposable?.dispose()
    }
}