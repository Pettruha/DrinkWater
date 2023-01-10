package com.iarigo.water.ui.fragment_settings

import com.iarigo.water.repository.PreferencesRepository
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.User
import com.iarigo.water.storage.entity.Weight
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.text.DecimalFormat
import java.text.NumberFormat

class SettingsPresenter: SettingsContract.Presenter {

    private lateinit var fragmentView: SettingsContract.View
    private val subscriptions = CompositeDisposable()
    private var dbHelper: AppDatabase? = null
    private lateinit var preferences: PreferencesRepository

    override fun viewIsReady(view: SettingsContract.View) {
        fragmentView = view
        dbHelper = AppDatabase.getAppDataBase(view.getFragmentContext())
        preferences = PreferencesRepository(fragmentView.getApplication())
    }

    override fun destroy() {
        subscriptions.dispose()
    }

    /**
     * Set current values
     */
    override fun fillValues() {
        getGender()// gender
        getWeight()// вес

        // rate us
        if (preferences.getRateUs()) {
            fragmentView.hideRate()
        }
    }

    override fun getGender() {
        val subscribe = dbHelper?.userDao()?.getUser(preferences.getUserId())?.subscribeOn(
            Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ user: User ->
                fragmentView.setGender(user.gender)
            }, { error ->
                error.printStackTrace()
            })
        subscriptions.add(subscribe!!)
    }

    /**
     * Get current weight
     */
    override fun getWeight() {
        val subscribe = dbHelper?.weightDao()?.getLastWeightMaybe()?.subscribeOn(
            Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ weight: Weight ->
                fragmentView.setWeight(weight.weight) // set current weight
                getWater(weight.weight)// calculate water count
            }, { error ->
                error.printStackTrace()
            })
        subscriptions.add(subscribe!!)
    }

    /**
     * calculate water count
     */
    private fun getWater(weight: Double) {
        fragmentView.setWaterPersonal(preferences.getDrinkCountPersonal())
        if (preferences.getDrinkCountPersonal()) {// user water count
            fragmentView.setWaterCount(preferences.getDrinkCountPerDay().toString())
        } else {// calculate water count from weight
            val format: NumberFormat = DecimalFormat("#")
            fragmentView.setWaterCount(format.format(weight * 30))
        }

    }

    override fun saveWaterCountPersonal(personal: Boolean) {
        preferences.saveDrinkCountPersonal(personal)
    }

    override fun getWaterDaily() {
        fragmentView.setWaterCount(preferences.getDrinkCountPerDay().toString())
    }

    override fun saveRate() {
        preferences.saveRateUs()
        fragmentView.hideRate()
    }
}