package com.iarigo.water.ui.dialogFirstLaunch

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.util.Log
import com.iarigo.water.helper.Helper
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.User
import com.iarigo.water.storage.entity.Weight
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DialogPresenter: DialogContract.Presenter {
    private lateinit var dialogView: DialogContract.View
    private var appDatabase: AppDatabase? = null // БД
    private val subscriptions = CompositeDisposable()
    private lateinit var mSettings: SharedPreferences

    override fun viewIsReady(view: DialogContract.View) {
        dialogView = view
        appDatabase = AppDatabase.getAppDataBase(dialogView.getDialogContext())
        mSettings = dialogView.getDialogContext().getSharedPreferences("water", Context.MODE_PRIVATE)
    }

    /**
     * Проверям и сохраняем данные поль-ля
     */
    @SuppressLint("SimpleDateFormat")
    override fun saveUser(bundle: Bundle) {
        var errors : Boolean = false
        if (bundle.getDouble("weight") < 30 || bundle.getDouble("weight") > 300) {// вес больше 10 кг меньше 300
            errors = true
            dialogView.showWeightError()
        }

        if (!bundle.getBoolean("sex_women") && !bundle.getBoolean("sex_men")) {// пол выбран
            errors = true
            dialogView.showSexError()
        }
        // время пробуждения - время сна более 3-х часов
        val simpleDateFormat = SimpleDateFormat("hh:mm")

        val date1 = simpleDateFormat.parse("${getStringHourMinute(bundle.getInt("wakeup_time_hour"))}:${getStringHourMinute(bundle.getInt("wakeup_time_minute"))}")
        val date2 = simpleDateFormat.parse("${getStringHourMinute(bundle.getInt("go_bed_time_hour"))}:${getStringHourMinute(bundle.getInt("go_bed_time_minute"))}")

        val difference: Long = date2.time - date1.time
        val days = (difference / (1000 * 60 * 60 * 24)).toInt()
        val hours = ((difference - 1000 * 60 * 60 * 24 * days) / (1000 * 60 * 60)).toInt()
        // hours = if (hours < 0) -hours else hours

        if (hours < 0) {
            errors = true
            dialogView.showTimeError(0)
        } else if (hours < 3) {
            errors = true
            dialogView.showTimeError(1)
        }

        if (!errors) {

            // сохраняем поль-ля
            val user: User = User()
            user.sex = if (bundle.getBoolean("sex_women")) 0 else 1
            user.wakeUpHour = bundle.getInt("wakeup_time_hour")
            user.wakeUpMinute = bundle.getInt("wakeup_time_minute")
            user.bedHour = bundle.getInt("go_bed_time_hour")
            user.bedMinute = bundle.getInt("go_bed_time_minute")

            val subscribe = appDatabase?.userDao()?.insert(user)?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ id: Long ->
                    saveNext(id, bundle)
                }, { error ->
                    error.printStackTrace()
                })
            subscriptions.add(subscribe!!)
        }
    }

    /**
     * Продолжаем сохранять инфу о поль-ле
     */
    private fun saveNext(userId: Long, bundle: Bundle) {
        // сохраняем вес
        val calendar: Calendar = Calendar.getInstance()
        val weight: Weight = Weight()
        weight.createAt = calendar.timeInMillis
        weight.weight = bundle.getDouble("weight")

        val subscribe = appDatabase?.weightDao()?.insert(weight)?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ _: Long ->
            }, { error ->
                error.printStackTrace()
            })
        subscriptions.add(subscribe!!)

        val e = mSettings.edit()
        e.putLong(Helper.USER_ID, userId)
        e.putBoolean(Helper.FIRST_VISITED, true)
        val format: NumberFormat = DecimalFormat("#")
        e.putString(Helper.WATER_COUNT_PER_DAY, format.format(bundle.getDouble("weight") * 30))
        e.apply()

        dialogView.closeDialog(bundle)
    }

    /**
     * Строка час и минута в правильном формате
     */
    private fun getStringHourMinute(time: Int): String {
        var wHour = time.toString()
        if (time < 10) {
            wHour = "0$time"
        }
        return wHour
    }

    override fun destroy() {
        subscriptions.dispose() // очищаем потоки
    }

    /**
     * Пересчитываем норму воды от веса
     * 30 мл жидкости на 1 кг веса
     */
    override fun calculateWater(editable: Editable?) {
        val waterString = editable.toString()
        var water: Double = 0.0
        if (waterString != "") {
            water = editable.toString().toDouble() * 30
        }

        dialogView.setWaterCount(water)
    }
}