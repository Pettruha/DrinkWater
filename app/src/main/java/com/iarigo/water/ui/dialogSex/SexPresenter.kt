package com.iarigo.water.ui.dialogSex

import android.content.Context
import android.content.SharedPreferences
import com.iarigo.water.helper.Helper
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.User
import com.iarigo.water.ui.dialogWeight.WeightContract
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class SexPresenter: SexContract.Presenter {
    private lateinit var dialogView: SexContract.View
    private var appDatabase: AppDatabase? = null // БД
    private val subscriptions = CompositeDisposable()
    private lateinit var mSettings: SharedPreferences

    override fun viewIsReady(view: SexContract.View) {
        dialogView = view
        appDatabase = AppDatabase.getAppDataBase(dialogView.getDialogContext())
        mSettings = dialogView.getDialogContext().getSharedPreferences("water", Context.MODE_PRIVATE)
    }

    override fun destroy() {
        subscriptions.dispose() // очищаем потоки
    }

    override fun getSex() {
        val subscribe = appDatabase?.userDao()?.getUser(mSettings.getLong(Helper.USER_ID, 1L))?.subscribeOn(
            Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ user: User ->
                dialogView.setSex(user)
            }, { error ->
                error.printStackTrace()
            })
        subscriptions.add(subscribe!!)
    }

    override fun saveSex(user: User) {
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