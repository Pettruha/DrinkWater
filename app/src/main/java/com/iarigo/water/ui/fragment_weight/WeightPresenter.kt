package com.iarigo.water.ui.fragment_weight

import android.content.Context
import android.content.SharedPreferences
import com.github.mikephil.charting.data.Entry
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.Weight
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class WeightPresenter: WeightContract.Presenter {

    private lateinit var fragmentView: WeightContract.View
    private val subscriptions = CompositeDisposable()
    private var dbHelper: AppDatabase? = null // БД
    private lateinit var mSettings: SharedPreferences // настройки приложения

    override fun viewIsReady(view: WeightContract.View) {
        fragmentView = view
        dbHelper = AppDatabase.getAppDataBase(view.getFragmentContext())
        mSettings = view.getFragmentContext().getSharedPreferences("water",
            Context.MODE_PRIVATE
        )
    }

    override fun destroy() {
        subscriptions.dispose() // очищаем потоки
    }

    override fun getWeights() {
        val subscribe = dbHelper?.weightDao()?.getWeightsPeriod()?.subscribeOn(
            Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe ({ weightList: List<Weight> ->
                    fragmentView.setWeightLog(weightList)
                },
                { error -> error.printStackTrace() },
                {

                })
        subscriptions.add(subscribe!!)
    }

    override fun getCurrentWeight() {
        val subscribe = dbHelper?.weightDao()?.getLastWeight()?.subscribeOn(
            Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe ({ weight: Weight ->
                fragmentView.setCurrentWeight(weight)
            },
                { error -> error.printStackTrace() },
                {

                })
        subscriptions.add(subscribe!!)
    }

    /**
     * Отображаем график за весь период
     */
    override fun getGraph() {

        val subscribe = dbHelper?.weightDao()?.getWeightsGraph()?.subscribeOn(
            Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe ({ weight: List<Weight> ->
                createWeightData(weight)
            },
                { error -> error.printStackTrace() },
                {

                })
        subscriptions.add(subscribe!!)
    }

    private fun createWeightData(weight: List<Weight>) {
        val values: ArrayList<Entry> = ArrayList()

        for (i in weight) {
            values.add(
                Entry(i.createAt.toFloat(), i.weight.toFloat())
            )
        }
        fragmentView.registryGraph(values)
    }
}