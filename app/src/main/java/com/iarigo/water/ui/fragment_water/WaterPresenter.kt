package com.iarigo.water.ui.fragment_water

import android.annotation.SuppressLint
import com.github.mikephil.charting.data.BarEntry
import com.iarigo.water.repository.PreferencesRepository
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.Drinks
import com.iarigo.water.storage.entity.Water
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class WaterPresenter: WaterContract.Presenter {

    private lateinit var fragmentView: WaterContract.View
    private val subscriptions = CompositeDisposable()
    private var dbHelper: AppDatabase? = null
    private lateinit var preferences: PreferencesRepository
    private var currentDay: Long = 0L
    private var dateSystemFormat: DateFormat? = null // system date format
    private val dateShortSystemFormat = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "ddMM") // system date format
    private var barIndex = 0f
    private val dateValues: ArrayList<String> = ArrayList()

    override fun viewIsReady(view: WaterContract.View) {
        fragmentView = view
        dbHelper = AppDatabase.getAppDataBase(view.getFragmentContext())
        preferences = PreferencesRepository(fragmentView.getApplication())
        // today
        dateSystemFormat = android.text.format.DateFormat.getDateFormat(view.getFragmentContext()) // system date format
        val today = Calendar.getInstance()
        currentDay = today.timeInMillis
    }

    override fun destroy() {
        subscriptions.dispose()
    }

    /**
     * Set Current day
     */
    override fun setCurrentDay() {
        val date: Date = Date(currentDay)
        val todayDate: String? = dateSystemFormat?.format(date)
        fragmentView.setWaterDay(todayDate!!)
    }

    /**
     * Set new current day after graph clicked
     * TODO add check new/old year
     */
    override fun setNewCurrentDay(day: String) {
        val today = Calendar.getInstance()
        val year = today.get(Calendar.YEAR)
        val dateLSystemFormat = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "ddMM") // system date format
        val sdf = SimpleDateFormat(dateLSystemFormat, Locale.getDefault())
        try {
            val d: Date = sdf.parse(day) as Date
            today.time = d
            today.set(Calendar.YEAR, year)
            currentDay = today.timeInMillis

            if (compareDates(currentDay, Calendar.getInstance().timeInMillis) == 0) { // block Add water button for previous days
                fragmentView.showAddWater(false)
            } else {
                fragmentView.showAddWater(true)
            }

            setCurrentDay() // set current day to view
            getWaters() // show daily water log
        } catch (ex: ParseException) {
            ex.printStackTrace()
        }
    }

    /**
     * Add drink
     */
    override fun addDrink() {
        // get favorite drink type
        val subscribe = dbHelper?.drinksDao()?.get(preferences.getDrinkType())?.subscribeOn(
            Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe ({ drink: Drinks? ->
                addWaterCount(drink!!)
            },
                { error -> error.printStackTrace() },
                {

                })
        subscriptions.add(subscribe!!)
    }

    @SuppressLint("DiscouragedApi")
    private fun addWaterCount(drinks: Drinks) {
        val calendar: Calendar = Calendar.getInstance()
        val context = fragmentView.getFragmentContext()

        val water: Water = Water()
        water.countWater = (drinks.percent *  preferences.getDrinkCount().toDouble()) / 100.0
        water.countDrink = preferences.getDrinkCount().toDouble()
        if (drinks.system) {// system name
            water.drinkName = context.getString(context.resources.getIdentifier(drinks.name, "string", context.packageName))
        } else water.drinkName = drinks.name
        water.createAt = calendar.timeInMillis
        // day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 1)
        water.dayAt = calendar.timeInMillis

        saveWater(water) // save
    }

    private fun saveWater(water: Water) {
        val subscribe = dbHelper?.waterDao()?.insertWater(water)?.subscribeOn(
            Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ _ ->
                getWaters()// get new lists water
                getGraph()// recreate graph
            }, { error ->
                error.printStackTrace()
            })
        subscriptions.add(subscribe!!)
    }

    override fun getWaters() {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = currentDay // set current day
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
     * Water graph
     * Show graph for 7 days
     */
    override fun getGraph() {

        val calendar: Calendar = Calendar.getInstance()
        // last day
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)

        val dayEnd = calendar.timeInMillis

        // first day
        calendar.add(Calendar.DAY_OF_MONTH, -6)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val dayBegin = calendar.timeInMillis

        val subscribe = dbHelper?.waterDao()?.getAllWater(dayBegin, dayEnd)?.subscribeOn(
            Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe ({ water: List<Water> ->
                        createGraph(water, dayBegin, dayEnd)
                },
                { error -> error.printStackTrace() },
                {
                    val water: List<Water> = ArrayList()
                    createGraph(water, dayBegin, dayEnd)
                })
        subscriptions.add(subscribe!!)
    }

    private fun createGraph(water: List<Water>, dayBegin: Long, dayEnd: Long) {
        barIndex = 0f
        dateValues.clear()

        val values: ArrayList<BarEntry> = ArrayList()
        val dateFormat: SimpleDateFormat = SimpleDateFormat(dateShortSystemFormat, Locale.getDefault())

        val calendar = Calendar.getInstance()

        var lastCheckedDay: Long = dayBegin

        for (one in water) {

            if (compareDates(lastCheckedDay, one.dayAt) >= 0) {
                calendar.timeInMillis = one.dayAt
                calendar.set(Calendar.HOUR_OF_DAY, 12)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                values.add(
                    BarEntry(
                        barIndex,
                        one.countWater.toFloat()
                    )
                )
                // date array
                dateValues.add(dateFormat.format(calendar.timeInMillis))
                barIndex += 1.0f
            } else {// add empty value

                values.addAll(fillDates(lastCheckedDay, one.dayAt))// fill empty days

                // add date from database
                calendar.timeInMillis = one.dayAt
                calendar.set(Calendar.HOUR_OF_DAY, 12)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                values.add(
                    BarEntry(
                        barIndex,
                        one.countWater.toFloat()
                    )
                )
                dateValues.add(dateFormat.format(calendar.timeInMillis))
                barIndex += 1.0f
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1) // next day
            lastCheckedDay = calendar.timeInMillis
        }

        // add value to the range end
        while (compareDates(lastCheckedDay, dayEnd) <= 0) {
            calendar.timeInMillis = lastCheckedDay
            values.add(BarEntry(barIndex, 0F))
            dateValues.add(dateFormat.format(calendar.timeInMillis))
            barIndex += 1.0f

            // next day
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            lastCheckedDay = calendar.timeInMillis
        }

        if (values.size < 7) {
            calendar.set(Calendar.HOUR_OF_DAY, 12)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            var i = values.size

            while (i < 7) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                values.add(BarEntry(barIndex, 0F))

                dateValues.add(dateFormat.format(calendar.timeInMillis))
                barIndex += 1.0f
                i++
            }
        }

        fragmentView.showGraph(values, dateValues)
    }

    /**
     * Fill empty dates
     * TODO check year
     */
    private fun fillDates(startDay: Long, endDay: Long): ArrayList<BarEntry> {
        val values: ArrayList<BarEntry> = ArrayList()
        val calendar = Calendar.getInstance()
        val dateFormat: SimpleDateFormat = SimpleDateFormat(dateShortSystemFormat, Locale.getDefault())

        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        var currentDay = startDay

        while (!formatter.format(currentDay).equals(formatter.format(endDay))) {
            calendar.timeInMillis = currentDay
            calendar.set(Calendar.HOUR_OF_DAY, 12)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            values.add(
                BarEntry(
                    barIndex,
                    0F
                )
            )
            dateValues.add(dateFormat.format(calendar.timeInMillis))
            // next day
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            currentDay = calendar.timeInMillis
            barIndex += 1.0f
        }

        return values
    }

    /**
     * Compare 2 dates
     */
    private fun compareDates(lastCheckedDay: Long, lastDay: Long): Int {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val date1 = formatter.parse(formatter.format(lastCheckedDay))
        val date2 = formatter.parse(formatter.format(lastDay))

        if (date1 != null) {
            try {
                return date1.compareTo(date2)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return 0
    }
}