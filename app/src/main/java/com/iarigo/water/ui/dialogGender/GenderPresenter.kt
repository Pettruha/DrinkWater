package com.iarigo.water.ui.dialogGender

import com.iarigo.water.repository.PreferencesRepository
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.User
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class GenderPresenter: GenderContract.Presenter {
    private lateinit var dialogView: GenderContract.View
    private var appDatabase: AppDatabase? = null // Database
    private val subscriptions = CompositeDisposable()
    private lateinit var preferences: PreferencesRepository

    override fun viewIsReady(view: GenderContract.View) {
        dialogView = view
        appDatabase = AppDatabase.getAppDataBase(dialogView.getDialogContext())
        preferences = PreferencesRepository(dialogView.getApplication())
    }

    override fun destroy() {
        subscriptions.dispose()
    }

    override fun getGender() {
        val subscribe = appDatabase?.userDao()?.getUser(preferences.getUserId())?.subscribeOn(
            Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ user: User ->
                dialogView.setGender(user)
            }, { error ->
                error.printStackTrace()
            })
        subscriptions.add(subscribe!!)
    }

    override fun saveGender(user: User) {
        val subscribe = appDatabase?.userDao()?.update(user)?.subscribeOn(
            Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ _ ->
                dialogView.closeDialog()
            }, { error ->
                error.printStackTrace()
            })
        subscriptions.add(subscribe!!)
    }
}