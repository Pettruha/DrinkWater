package com.iarigo.water.ui.fragment_notify

import android.content.Context
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import com.iarigo.water.R
import com.iarigo.water.base.BasePresenter
import com.iarigo.water.helper.Helper
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.User
import com.iarigo.water.ui.main.MainContract
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

class NotifyPresenter: NotifyContract.Presenter {

    private lateinit var fragmentView: NotifyContract.View
    private val subscriptions = CompositeDisposable()
    private var dbHelper: AppDatabase? = null // БД
    private lateinit var mSettings: SharedPreferences // настройки приложения

    override fun viewIsReady(view: NotifyContract.View) {
        fragmentView = view
        dbHelper = AppDatabase.getAppDataBase(view.getFragmentContext())
        mSettings = view.getFragmentContext().getSharedPreferences("water",
            Context.MODE_PRIVATE
        )
    }

    override fun destroy() {
        subscriptions.dispose() // очищаем потоки
    }

    override fun getParams() {
        fragmentView.setNormaOver(mSettings.getBoolean(Helper.NOTIFY_WATER_OVER, false))
        getPeriod()
        getFreq()
        fragmentView.setNotifyOn(mSettings.getBoolean(Helper.NOTIFY_ON, true))
    }

    override fun getSound(): String {
        var string = mSettings.getString(Helper.NOTIFY_SOUND, "notification_sound")
        if (string == null)
            string = "notification_sound"
        return string
    }

    private fun getPeriod() {
        val subscribe = dbHelper?.userDao()?.getUser(mSettings.getLong(Helper.USER_ID, 1L))?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ user: User ->
                val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, user.wakeUpHour)
                calendar.set(Calendar.MINUTE, user.wakeUpMinute)

                val timeWakeUp: String = formatter.format(calendar.time)

                calendar.set(Calendar.HOUR_OF_DAY, user.bedHour)
                calendar.set(Calendar.MINUTE, user.bedMinute)
                val timeGoBed: String = formatter.format(calendar.time)

                fragmentView.setPeriod(timeWakeUp, timeGoBed)

            }, { error ->
                error.printStackTrace()
            })
        subscriptions.add(subscribe!!)
    }

    override fun getFreq() {
        val hour = mSettings.getInt(Helper.NOTIFY_FREQ_HOUR, 1)
        val minute = mSettings.getInt(Helper.NOTIFY_FREQ_MINUTE, 0)
        var string = ""
        if (hour == 0) {
            string = "$minute" + fragmentView.getFragmentContext().getString(R.string.notify_freq_value_min)
        } else {
            string = "$hour" + fragmentView.getFragmentContext().getString(R.string.notify_freq_value_hour)
            string += " $minute" + fragmentView.getFragmentContext().getString(R.string.notify_freq_value_min)
        }

        fragmentView.setFreq(string)
    }

    override fun saveOver(over: Boolean) {
        val e = mSettings.edit()
        e.putBoolean(Helper.NOTIFY_WATER_OVER, over)
        e.apply()
    }

    override fun saveNotifyOn(on: Boolean) {
        val e = mSettings.edit()
        e.putBoolean(Helper.NOTIFY_ON, on)
        e.apply()
    }

    override fun saveSound(sound: String) {
        val e = mSettings.edit()
        e.putString(Helper.NOTIFY_SOUND, sound)
        e.apply()
    }

    override fun saveWaterPeriod(
        wakeUpHour: Int,
        wakeUpMinute: Int,
        goBedHour: Int,
        goBedMinute: Int
    ) {
        val subscribe = dbHelper?.userDao()?.getUser(mSettings.getLong(Helper.USER_ID, 1L))?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ user: User ->
                user.wakeUpHour = wakeUpHour
                user.wakeUpMinute = wakeUpMinute
                user.bedHour = goBedHour
                user.bedMinute = goBedMinute

                updateUser(user)
            }, { error ->
                error.printStackTrace()
            })
        subscriptions.add(subscribe!!)
    }

    private fun updateUser(user: User) {
        val subscribe = dbHelper?.userDao()?.update(user)?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ _ ->
            }, { error ->
                error.printStackTrace()
            })
        subscriptions.add(subscribe!!)
    }
}