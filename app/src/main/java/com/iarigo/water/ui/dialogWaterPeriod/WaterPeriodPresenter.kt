package com.iarigo.water.ui.dialogWaterPeriod

import android.annotation.SuppressLint
import android.os.Bundle
import com.iarigo.water.repository.PreferencesRepository
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.User
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat

class WaterPeriodPresenter: WaterPeriodContract.Presenter {
    private lateinit var dialogView: WaterPeriodContract.View
    private var appDatabase: AppDatabase? = null // Database
    private var disposable: Disposable? = null
    private val subscriptions = CompositeDisposable()
    private lateinit var preferences: PreferencesRepository

    override fun viewIsReady(view: WaterPeriodContract.View) {
        dialogView = view
        appDatabase = AppDatabase.getAppDataBase(dialogView.getDialogContext())
        preferences = PreferencesRepository(dialogView.getApplication())
        getPeriod()
    }

    private fun getPeriod() {
        val subscribe = appDatabase?.userDao()?.getUser(preferences.getUserId())?.subscribeOn(
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

        val simpleDateFormat = SimpleDateFormat("hh:mm")

        val date1 = simpleDateFormat.parse("${user.wakeUpHour}:${user.wakeUpMinute}")
        val date2 = simpleDateFormat.parse("${user.bedHour}:${user.bedMinute}")

        val difference: Long = (date2?.time ?: 0) - (date1?.time ?: 0)
        val days = (difference / (1000 * 60 * 60 * 24)).toInt()
        val hours = ((difference - 1000 * 60 * 60 * 24 * days) / (1000 * 60 * 60)).toInt()

        if (hours < 0) {
            errors = true
            dialogView.showTimeError(0)
        } else if (hours < 3) {
            errors = true
            dialogView.showTimeError(1)
        }

        if (!errors) {
            // save user
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
        subscriptions.dispose()
        disposable?.dispose()
    }
}