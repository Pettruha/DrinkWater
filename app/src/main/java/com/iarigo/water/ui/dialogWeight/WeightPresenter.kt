package com.iarigo.water.ui.dialogWeight

import android.content.Context
import android.content.SharedPreferences
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.Weight
import io.reactivex.Flowable.just
import io.reactivex.Observable.just
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

class WeightPresenter: WeightContract.Presenter {
    private lateinit var dialogView: WeightContract.View
    private var appDatabase: AppDatabase? = null // БД
    private val subscriptions = CompositeDisposable()
    private lateinit var mSettings: SharedPreferences

    override fun viewIsReady(view: WeightContract.View) {
        dialogView = view
        appDatabase = AppDatabase.getAppDataBase(dialogView.getDialogContext())
        mSettings = dialogView.getDialogContext().getSharedPreferences("water", Context.MODE_PRIVATE)
    }

    override fun destroy() {
        subscriptions.dispose() // очищаем потоки
    }

    override fun getCurrentWeight() {
        val subscribe = appDatabase?.weightDao()?.getLastWeight()?.subscribeOn(
            Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe ({ weight: Weight ->
                dialogView.setCurrentWeight(weight)
            },
                { error -> error.printStackTrace() },
                {

                })
        subscriptions.add(subscribe!!)
    }

    override fun saveWeight(weight: String) {
        var errors : Boolean = false
        val weightDouble: Double = weight.toDouble()
        if (weightDouble < 30 || weightDouble > 300) {// вес больше 10 кг меньше 300
            errors = true
            dialogView.showWeightError()
        }

        if (!errors) {
            val subscribe = appDatabase?.weightDao()?.getLastWeight()?.subscribeOn(
                Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe ({ currentWeight: Weight ->
                    saveWeight2(weightDouble, currentWeight)
                },
                    { error -> error.printStackTrace() },
                    {

                    })
            subscriptions.add(subscribe!!)
        }
    }

    private fun saveWeight2(weight: Double, currentWeight: Weight) {

        val oldFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = currentWeight.createAt

        if (oldFormatter.format(Date()) == oldFormatter.format(calendar.time)) {// обновляем
            currentWeight.weight = weight

            val subscribe = appDatabase?.weightDao()?.update(currentWeight)?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ _: Int ->
                    dialogView.closeDialog()
                }, { error ->
                    error.printStackTrace()
                })
            subscriptions.add(subscribe!!)
        } else {// новое взвешивание
            val newWeight: Weight = Weight()
            val newCalendar: Calendar = Calendar.getInstance()
            newWeight.createAt = newCalendar.timeInMillis
            newWeight.weight = weight

            val subscribe = appDatabase?.weightDao()?.insert(newWeight)?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ _: Long ->
                    dialogView.closeDialog()
                }, { error ->
                    error.printStackTrace()
                })
            subscriptions.add(subscribe!!)
        }

    }
}