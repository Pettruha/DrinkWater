package com.iarigo.water.ui.fragment_water

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.github.mikephil.charting.data.BarEntry
import com.iarigo.water.base.BasePresenter
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.Water
import com.iarigo.water.storage.entity.Weight
import com.iarigo.water.ui.fragment_weight.WeightContract
import com.iarigo.water.ui.main.MainContract
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.channels.consumesAll
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class WaterPresenter: WaterContract.Presenter {

    private lateinit var fragmentView: WaterContract.View
    private val subscriptions = CompositeDisposable()
    private var dbHelper: AppDatabase? = null // БД
    private lateinit var mSettings: SharedPreferences // настройки приложения

    override fun viewIsReady(view: WaterContract.View) {
        fragmentView = view
        dbHelper = AppDatabase.getAppDataBase(view.getFragmentContext())
        mSettings = view.getFragmentContext().getSharedPreferences("water",
            Context.MODE_PRIVATE
        )
    }

    override fun destroy() {
        subscriptions.dispose() // очищаем потоки
    }

    override fun getWaters() {
        val calendar: Calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val dayBegin = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)

        val dayEnd = calendar.timeInMillis

        val subscribe = dbHelper?.waterDao()?.getWaterCountToday(dayBegin, dayEnd)?.subscribeOn(
            Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe ({ waterList: List<Water>? ->
                    fragmentView.setWaterLog(waterList!!)
                },
                { error -> error.printStackTrace() },
                {

                })
        subscriptions.add(subscribe!!)
    }

    /**
     * График воды
     * Показываем за 7 дней
     */
    override fun getGraph() {

        val calendar: Calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -5)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val dayBegin = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, +6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)

        val dayEnd = calendar.timeInMillis

        val subscribe = dbHelper?.waterDao()?.getAllWater(dayBegin, dayEnd)?.subscribeOn(
            Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe ({ water: List<Water> ->
                    createGraph(water)
                },
                { error -> error.printStackTrace() },
                {
                    val water: List<Water> = ArrayList()
                    createGraph(water)
                })
        subscriptions.add(subscribe!!)
    }

    private fun createGraph(water: List<Water>) {
        val values: ArrayList<BarEntry> = ArrayList()


        val calendar = Calendar.getInstance()

        for (one in water) {

            calendar.timeInMillis = one.dayAt
            calendar.set(Calendar.HOUR_OF_DAY, 12)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            values.add(BarEntry(TimeUnit.MILLISECONDS.toDays(calendar.timeInMillis).toFloat(), one.countWater.toFloat()))
        }


        if (water.size < 7) {
            calendar.set(Calendar.HOUR_OF_DAY, 12)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            var i = water.size

            while (i < 7) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                values.add(BarEntry(TimeUnit.MILLISECONDS.toDays(calendar.timeInMillis).toFloat(), 0F))
                i++
            }
        }

        fragmentView.registryGraph(values)
    }
}