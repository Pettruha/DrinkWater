package com.iarigo.water.ui.fragment_main

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.util.Log
import com.iarigo.water.helper.Helper
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.Drinks
import com.iarigo.water.storage.entity.User
import com.iarigo.water.storage.entity.Water
import com.iarigo.water.storage.entity.Weight
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*


class MainPresenter: MainContract.Presenter {

    private lateinit var fragmentView: MainContract.View
    private val subscriptions = CompositeDisposable()
    private var dbHelper: AppDatabase? = null // БД
    private lateinit var mSettings: SharedPreferences // настройки приложения

    override fun viewIsReady(view: MainContract.View) {
        fragmentView = view
        dbHelper = AppDatabase.getAppDataBase(view.getFragmentContext())
        mSettings = view.getFragmentContext().getSharedPreferences("water",
            Context.MODE_PRIVATE
        )
        getWaterCount()
        setWaterCountPerOnce()
    }

    override fun destroy() {
        subscriptions.dispose() // очищаем потоки
    }

    private fun getWaterCount() {
        if (!mSettings.getBoolean(Helper.WATER_COUNT_PERSONAL, false)) {
            getWeight()// расчет по весу
        } else {// собственная норма воды
            val water = mSettings.getString(Helper.WATER_COUNT_PER_DAY, "1800")
            if (water != null)
                getWaterToday(water.toInt())
            else getWaterToday(1800)
        }
    }

    /**
     * текущий вес
     */
    private fun getWeight() {
        val subscribe = dbHelper?.weightDao()?.getLastWeight()?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ weight: Weight ->
                getWaterToday((weight.weight * 30).toInt())
            }, { error ->
                error.printStackTrace()
            }, {
                getWaterToday(1800) })
        subscriptions.add(subscribe!!)
    }

    /**
     * Кол-во выпитой воды
     */
    private fun getWaterToday(waterCountNorma: Int) {
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

        val subscribe = dbHelper?.waterDao()?.getWaterCount(dayBegin, dayEnd)?.subscribeOn(
            Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe ({ water: Double ->
                fragmentView.setWaterCount(waterCountNorma, water.toInt()) // кол-во воды цифровое
                waterCat(waterCountNorma, water.toInt())// кол-во воды кошачье
                },
                { error -> error.printStackTrace() },
                {
                    fragmentView.setWaterCount(waterCountNorma, 0) // кол-во воды цифровое
                    waterCat(waterCountNorma, 0)// кол-во воды кошачье
                })
        subscriptions.add(subscribe!!)
    }

    /**
     * кошачье кол-во воды
     */
    private fun waterCat(waterCountNorma: Int, waterCount: Int) {

        val percent: Float = (waterCount.toFloat() / waterCountNorma.toFloat()) * 100

        when (percent.toInt()) {
            0 -> {
                fragmentView.setCat(1)
            }
            in 1..30 -> {
                fragmentView.setCat(2)
            }
            in 31..55 -> {
                fragmentView.setCat(3)
            }
            in 56..85 -> {
                fragmentView.setCat(4)
            }
            in 85..99 -> {
                fragmentView.setCat(5)
            }
            else -> {
                fragmentView.setCat(6)
            }
        }
    }

    /**
     * Выпили воды
     */
    override fun addWater() {

        val subscribe = dbHelper?.drinksDao()?.getDrink(mSettings.getLong(Helper.WATER_DRINK_SELECTED, 1L))?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ drink: Drinks ->
                addWaterCount(drink)
            }, { error ->
                error.printStackTrace()
            }, {
                val drink = Drinks()
                drink.name = "Вода"
                drink.percent = 100
                addWaterCount(drink)
            })
        subscriptions.add(subscribe!!)
    }

    private fun addWaterCount(drinks: Drinks) {
        val calendar: Calendar = Calendar.getInstance()

        val water: Water = Water()
        water.countWater = (drinks.percent *  mSettings.getString(Helper.WATER_COUNT, "200")!!.toDouble()) / 100.0
        water.countDrink = mSettings.getString(Helper.WATER_COUNT, "200")!!.toDouble()
        water.drinkName = drinks.name
        water.createAt = calendar.timeInMillis
        // день
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 1)
        water.dayAt = calendar.timeInMillis

        val subscribe = dbHelper?.waterDao()?.insert(water)?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ _ ->
                getWaterCount()// пересчитываем кол-во и картинки
            }, { error ->
                error.printStackTrace()
            })
        subscriptions.add(subscribe!!)
    }

    /**
     * Кол-во воды за один раз
     */
    override fun setWaterCountPerOnce() {
        fragmentView.setWaterPerOnce(mSettings.getString(Helper.WATER_COUNT, "200")!!.toInt())
    }
}
