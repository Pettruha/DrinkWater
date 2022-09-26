package com.iarigo.water.ui.main

import android.content.Context
import android.content.SharedPreferences
import com.iarigo.water.AlarmReceiver
import com.iarigo.water.base.BasePresenter
import com.iarigo.water.helper.Helper
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.ui.dialogWaterInterval.DialogWaterInterval
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class MainPresenter: BasePresenter<MainContract.View>(), MainContract.Presenter {

    private val subscriptions = CompositeDisposable()
    private var dbHelper: AppDatabase? = null // БД
    private lateinit var mSettings: SharedPreferences // настройки приложения
    private var disposable: Disposable? = null // вызов subscribe

    override fun viewIsReady() {
        dbHelper = AppDatabase.getAppDataBase(getView()!!.getContext())
        mSettings = getView()!!.getContext().getSharedPreferences("water", Context.MODE_PRIVATE)
    }

    override fun destroy() {
        subscriptions.dispose() // очищаем потоки
        disposable?.dispose()
    }

    /**
     * Восстанавливаем текущий фрагмент
     */
    override fun onResume() {
        firstLaunch() // первый запуск приложения
    }

    /**
     * Первый запуск приложения
     */
    private fun firstLaunch() {
        val hasVisited =
            mSettings.getBoolean(Helper.FIRST_VISITED, false) // первый запуск

        if (!hasVisited) {
            getView()?.showFirstLaunch()// показываем стартовый экран

            // NotificationChannel
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                AlarmReceiver.registryNotificationChannel(getView()!!.getContext())
            }

        } else {
            // оценивание
            Thread { gradeUs() }.start()
        }
    }

    /**
     * Сохраняем, первый запуск завершен
     */
    override fun saveFirstLaunch() {
        // ставим метку, что первый шаг пройден
        val e = mSettings.edit()
        e.putBoolean(Helper.FIRST_VISITED, true)
        // сохраняем дату первого запуска
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis
        e.putLong("startDate", today)
        e.apply()// сохраняем
    }

    /**
     * Проверка, не пора ли нас оценить
     */
    private fun gradeUs() {
        val mSettings = getView()!!.getContext().getSharedPreferences("water", Context.MODE_PRIVATE)
        if (!mSettings.getBoolean("graded", false)) {// если еще не оценивал
            val calendar = Calendar.getInstance()
            val today = calendar.timeInMillis
            // проверяем кол-во дней, которые прошли после "Напомнить позже"
            val rememberDay = mSettings.getLong("grade_later", 0) // дата, если было нажато напомнить позже

            if (rememberDay < today) {
                // проверяем кол-во дней пользования приложением
                val firstDay = mSettings.getLong("startDate", 0)
                val diff = today - firstDay
                val diffDays = diff / (24 * 60 * 60 * 1000)
                if (diffDays > 3) {
                    getView()?.showGradeUs()
                }
            }
        }
    }
}