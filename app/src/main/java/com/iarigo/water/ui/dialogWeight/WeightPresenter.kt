package com.iarigo.water.ui.dialogWeight

import com.iarigo.water.repository.PreferencesRepository
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.Weight
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

class WeightPresenter: WeightContract.Presenter {
    private lateinit var dialogView: WeightContract.View
    private var appDatabase: AppDatabase? = null // Database
    private val subscriptions = CompositeDisposable()
    private lateinit var preferences: PreferencesRepository

    override fun viewIsReady(view: WeightContract.View) {
        dialogView = view
        appDatabase = AppDatabase.getAppDataBase(dialogView.getDialogContext())
        preferences = PreferencesRepository(dialogView.getApplication())
    }

    override fun destroy() {
        subscriptions.dispose()
    }

    override fun getCurrentWeight() {
        val subscribe = appDatabase?.weightDao()?.getLastWeightMaybe()?.subscribeOn(
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
        if (weightDouble < 30 || weightDouble > 300) {// weight between 10 ~ 300
            errors = true
            dialogView.showWeightError()
        }

        if (!errors) {
            val subscribe = appDatabase?.weightDao()?.getLastWeightMaybe()?.subscribeOn(
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

        if (oldFormatter.format(Date()) == oldFormatter.format(calendar.time)) {// update today weight
            currentWeight.weight = weight

            val subscribe = appDatabase?.weightDao()?.update(currentWeight)?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ _: Int ->
                    dialogView.closeDialog()
                }, { error ->
                    error.printStackTrace()
                })
            subscriptions.add(subscribe!!)
        } else {// new weight
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