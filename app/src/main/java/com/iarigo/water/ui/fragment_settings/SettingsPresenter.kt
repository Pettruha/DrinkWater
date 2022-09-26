package com.iarigo.water.ui.fragment_settings

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.iarigo.water.base.BasePresenter
import com.iarigo.water.helper.Helper
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.User
import com.iarigo.water.storage.entity.Weight
import com.iarigo.water.ui.main.MainContract
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.text.DecimalFormat
import java.text.NumberFormat

class SettingsPresenter: SettingsContract.Presenter {

    private lateinit var fragmentView: SettingsContract.View
    private val subscriptions = CompositeDisposable()
    private var dbHelper: AppDatabase? = null // БД
    private lateinit var mSettings: SharedPreferences // настройки приложения

    override fun viewIsReady(view: SettingsContract.View) {
        fragmentView = view
        dbHelper = AppDatabase.getAppDataBase(view.getFragmentContext())
        mSettings = view.getFragmentContext().getSharedPreferences("water",
            Context.MODE_PRIVATE
        )
    }

    override fun destroy() {
        subscriptions.dispose() // очищаем потоки
    }

    override fun fillValues() {
        getSex()// пол
        getWeight()// вес

        // напиток

        // оценить
        if (mSettings.getBoolean(Helper.RATE, false)) {
            fragmentView.hideRate()
        }
    }

    override fun getSex() {
        val subscribe = dbHelper?.userDao()?.getUser(mSettings.getLong(Helper.USER_ID, 1L))?.subscribeOn(
            Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ user: User ->
                fragmentView.setSex(user.sex)
            }, { error ->
                error.printStackTrace()
            })
        subscriptions.add(subscribe!!)
    }

    override fun getWeight() {
        val subscribe = dbHelper?.weightDao()?.getLastWeight()?.subscribeOn(
            Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ weight: Weight ->
                fragmentView.setWeight(weight.weight)
                getWater(weight.weight)// норма воды
            }, { error ->
                error.printStackTrace()
            })
        subscriptions.add(subscribe!!)
    }

    /**
     * Норма воды
     */
    private fun getWater(weight: Double) {
        fragmentView.setWaterPersonal(mSettings.getBoolean(Helper.WATER_COUNT_PERSONAL, false))
        if (mSettings.getBoolean(Helper.WATER_COUNT_PERSONAL, false)) {// собственная норма
            val water = mSettings.getString(Helper.WATER_COUNT_PER_DAY, "1800")
            if (water != null)
                fragmentView.setWaterCount(water)
            else fragmentView.setWaterCount("1800")
        } else {// от веса
            val format: NumberFormat = DecimalFormat("#")
            fragmentView.setWaterCount(format.format(weight * 30))
        }

    }

    override fun saveWaterCountPersonal(personal: Boolean) {
        val e = mSettings.edit()
        e.putBoolean(Helper.WATER_COUNT_PERSONAL, personal)
        e.apply()
    }

    override fun getWaterDaily() {
        val water = mSettings.getString(Helper.WATER_COUNT_PER_DAY, "1800")
        if (water != null)
            fragmentView.setWaterCount(water)
        else fragmentView.setWaterCount("1800")
    }

}