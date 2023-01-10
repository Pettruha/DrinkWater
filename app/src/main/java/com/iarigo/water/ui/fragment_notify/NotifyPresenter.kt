package com.iarigo.water.ui.fragment_notify

import com.iarigo.water.R
import com.iarigo.water.repository.PreferencesRepository
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.User
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

class NotifyPresenter: NotifyContract.Presenter {

    private lateinit var fragmentView: NotifyContract.View
    private val subscriptions = CompositeDisposable()
    private var dbHelper: AppDatabase? = null
    private lateinit var preferences: PreferencesRepository

    override fun viewIsReady(view: NotifyContract.View) {
        fragmentView = view
        dbHelper = AppDatabase.getAppDataBase(view.getFragmentContext())
        preferences = PreferencesRepository(view.getApplication())
    }

    override fun destroy() {
        subscriptions.dispose()
    }

    override fun getParams() {
        fragmentView.setNormaOver(preferences.getNormaOver())
        getPeriod()
        getFreq()
        fragmentView.setNotifyOn(preferences.notify())
    }

    override fun getSound(): String {
        var string = preferences.getSound()
        if (string == null)
            string = "notification_sound"
        return string
    }

    private fun getPeriod() {
        val subscribe = dbHelper?.userDao()?.getUser(preferences.getUserId())?.subscribeOn(Schedulers.io())
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
        val hour = preferences.getWaterIntervalHour()
        val minute = preferences.getWaterIntervalMinute()
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
        preferences.saveNormaOver(over)
    }

    override fun saveNotifyOn(on: Boolean) {
        preferences.saveNotify(on)
    }

    override fun saveWaterPeriod(
        wakeUpHour: Int,
        wakeUpMinute: Int,
        goBedHour: Int,
        goBedMinute: Int
    ) {
        val subscribe = dbHelper?.userDao()?.getUser(preferences.getUserId())?.subscribeOn(Schedulers.io())
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