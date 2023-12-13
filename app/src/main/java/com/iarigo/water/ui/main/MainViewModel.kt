package com.iarigo.water.ui.main

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.text.Editable
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.iarigo.water.AlarmReceiver
import com.iarigo.water.R
import com.iarigo.water.helper.Event
import com.iarigo.water.repository.*
import com.iarigo.water.storage.entity.*
import kotlinx.coroutines.*
import java.text.*
import java.util.*
import kotlin.collections.ArrayList

class MainViewModel(application: Application): AndroidViewModel(application) {

    private var preferencesRepository: PreferencesRepository = PreferencesRepository(application)
    private var userRepository: UserRepository = UserRepository(application)
    private var weightRepository: WeightRepository = WeightRepository(application)
    private var waterRepository: WaterRepository = WaterRepository(application)
    private var drinkRepository: DrinksRepository = DrinksRepository(application)

    // dialog
    private val statusDialog = MutableLiveData<Event<Int>>()
    val dialog: LiveData<Event<Int>>
        get() = statusDialog

    // DialogFirstLaunch WeightError
    private val dialogWeightError = MutableLiveData<Event<String>>()
    val weightError: LiveData<Event<String>>
        get() = dialogWeightError

    // DialogFirstLaunch GenderError
    private val dialogGenderError = MutableLiveData<Event<String>>()
    val genderError: LiveData<Event<String>>
        get() = dialogGenderError

    // DialogFirstLaunch TimeError
    private val dialogTimeError = MutableLiveData<Event<String>>()
    val timeError: LiveData<Event<String>>
        get() = dialogTimeError

    // DialogFirstLaunch water count
    private val dialogWaterCount = MutableLiveData<Event<Double>>()
    val waterCount: LiveData<Event<Double>>
        get() = dialogWaterCount

    // close DialogFirstLaunch
    private val closeFirstLaunchDialog = MutableLiveData<Event<Bundle>>()
    val closeDialog: LiveData<Event<Bundle>>
        get() = closeFirstLaunchDialog

    /**
     * Check first app launch
     */
    fun firstLaunch() {
        if (!preferencesRepository.firstLaunch()) {
            statusDialog.value = Event(1)

            // NotificationChannel
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                AlarmReceiver.registryNotificationChannel(getApplication())
            }
        } // Here maybe grade us
    }

    /**
     * Calc amount of water by user weight
     *
     * 30 ml of drink per 1 kg of body weight
     * @param editable - EditText value
     */
    fun calculateWater(editable: Editable?) {
        val waterString = editable.toString()
        var water = 0.0
        if (waterString != "") {
            water = waterString.toDouble() * 30
        }
        dialogWaterCount.value = Event(water)
    }

    /**
     * Dialog First Launch.
     * Check and save user data
     * @param bundle - user data
     */
    @SuppressLint("SimpleDateFormat")
    fun saveUser(bundle: Bundle) {
        var errors = false
        if (bundle.getDouble("weight") < 30 || bundle.getDouble("weight") > 300) {// weight between 30 - 300 kg
            errors = true
            dialogWeightError.value = Event(getApplication<Application>().getString(R.string.dialog_weight_error))
        }

        if (!bundle.getBoolean("gender_woman") && !bundle.getBoolean("gender_man")) {// gender selected
            errors = true
            dialogGenderError.value = Event(getApplication<Application>().getString(R.string.dialog_gender_error))
        }
        // wake up time and time to bed less that 3 hours
        val simpleDateFormat = SimpleDateFormat("hh:mm")

        val date1 = simpleDateFormat.parse("${getStringHourMinute(bundle.getInt("wakeup_time_hour"))}:${getStringHourMinute(bundle.getInt("wakeup_time_minute"))}")
        val date2 = simpleDateFormat.parse("${getStringHourMinute(bundle.getInt("go_bed_time_hour"))}:${getStringHourMinute(bundle.getInt("go_bed_time_minute"))}")

        val difference: Long = date2.time - date1.time
        val days = (difference / (1000 * 60 * 60 * 24)).toInt()
        val hours = ((difference - 1000 * 60 * 60 * 24 * days) / (1000 * 60 * 60)).toInt()
        // hours = if (hours < 0) -hours else hours

        if (hours < 0) {
            errors = true
            dialogTimeError.value = Event(getApplication<Application>().getString(R.string.dialog_wakeup_time_error))
        } else if (hours < 3) {
            errors = true
            dialogTimeError.value = Event(getApplication<Application>().getString(R.string.dialog_wakeup_time_error_2))
        }

        if (!errors) {
            // save user
            val user = User()
            user.gender = if (bundle.getBoolean("gender_woman")) 0 else 1
            user.wakeUpHour = bundle.getInt("wakeup_time_hour")
            user.wakeUpMinute = bundle.getInt("wakeup_time_minute")
            user.bedHour = bundle.getInt("go_bed_time_hour")
            user.bedMinute = bundle.getInt("go_bed_time_minute")

            saveNewUser(user, bundle)// save user
        }
    }

    /**
     * First launch
     * Save user parameters
     * @param user - new user
     * @param bundle - user data
     */
    private fun saveNewUser(user: User, bundle: Bundle) = viewModelScope.launch {
        val insertedId = userRepository.insert(user)!!
        saveNext(insertedId, bundle)
    }

    /**
     * Save user weight and user info
     * @param userId - user id
     * @param bundle - user data
     */
    private fun saveNext(userId: Long, bundle: Bundle) {
        // save weight
        val calendar: Calendar = Calendar.getInstance()
        val weight = Weight()
        weight.createAt = calendar.timeInMillis
        weight.weight = bundle.getDouble("weight")

        saveNewWeight(weight)// save current weight

        // save user info
        val format: NumberFormat = DecimalFormat("#")
        val waterCount = format.format(bundle.getDouble("weight") * 30).toInt()
        preferencesRepository.saveUserInfo(userId, waterCount)

        closeFirstLaunchDialog.value = Event(bundle)// close dialog First Launch
    }

    private fun saveNewWeight(weight: Weight) = viewModelScope.launch {
        weightRepository.insertFirstTime(weight)
    }

    /**
     * Hour/Minute in format
     */
    private fun getStringHourMinute(time: Int): String {
        var wHour = time.toString()
        if (time < 10) {
            wHour = "0$time"
        }
        return wHour
    }

    /**
     * First launch done
     */
    fun saveFirstLaunch() {
        preferencesRepository.saveFirstLaunch()
    }

    /**
     * Notification save sound. API < 26
     */
    fun saveSound(sound: String) {
        preferencesRepository.saveSound(sound)
    }

    ///////////////////////////////////////Main Fragment//////////////////////////////////////////////////////////

    // MainFragment Drink count
    private val _mainFragmentDrinkCount = MutableLiveData<Event<Bundle>>()
    val mainFragmentDrinkCount: LiveData<Event<Bundle>>
        get() = _mainFragmentDrinkCount

    // MainFragment Drink count view
    private val _mainFragmentDrinkCountView = MutableLiveData<Event<Int>>()
    val mainFragmentDrinkCountView: LiveData<Event<Int>>
        get() = _mainFragmentDrinkCountView

    // MainFragment Drink count per once
    private val _drinkCountPerOnce = MutableLiveData<Event<Int>>()
    val drinkCountPerOnce: LiveData<Event<Int>>
        get() = _drinkCountPerOnce

    // Drink added Update
    private val drinkAdded = MutableLiveData<Event<Boolean>>()

    /**
     * Calculate
     * - Count of water per day
     * - Count of water norma per day
     */
    val getWaterLiveData: LiveData<Bundle> = MediatorLiveData<Bundle>().apply {
        val observer = Observer<Bundle> { value = it }
        addSource(drinkAdded) {
            addSource(getWeight()) { weight ->
                val today = getToday()
                getWaterCountPerDay(today.getLong("startDay"), today.getLong("endDay"), weight)
                addSource(
                    getWaterCountPerDay(
                        today.getLong("startDay"),
                        today.getLong("endDay"),
                        weight
                    ), observer
                )
            }
        }
        addSource(getWeight()) { weight ->
            val today = getToday()
            getWaterCountPerDay(today.getLong("startDay"), today.getLong("endDay"), weight)
            addSource(
                getWaterCountPerDay(
                    today.getLong("startDay"),
                    today.getLong("endDay"),
                    weight
                ), observer
            )
        }
    }

    /**
     * Current user weight
     */
    private fun getWeight(): LiveData<Int> {
        val resultWeight: MutableLiveData<Int> = MutableLiveData<Int>()
        viewModelScope.launch {
            var waterCountNorma = 1800
            val weight = weightRepository.lastWeight()
            if (weight != null) {
                waterCountNorma = (weight.weight * 30).toInt()
            }
            resultWeight.value = waterCountNorma
        }
        return resultWeight
    }

    /**
     * Start/End day
     */
    private fun getToday(): Bundle {
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

        val bundle = Bundle()
        bundle.putLong("startDay", dayBegin)
        bundle.putLong("endDay", dayEnd)

        return bundle
    }

    /**
     * Water norma per day
     */
    private fun getWaterCountPerDay(dayBegin: Long, dayEnd: Long, waterCountNorma: Int): LiveData<Bundle> {
        val result: MutableLiveData<Bundle> = MutableLiveData<Bundle>()
        viewModelScope.launch(Dispatchers.IO) {
            val waterCount = waterRepository.waterCount(dayBegin, dayEnd)
            val bundle = Bundle()
            if (waterCount != null) {
                bundle.putInt("waterCountNorma", waterCountNorma)
                bundle.putInt("waterCount", waterCount.toInt())
            } else {
                bundle.putInt("waterCountNorma", waterCountNorma)
                bundle.putInt("waterCount", 0)
            }
            result.postValue(bundle)
        }
        return result
    }

    /**
     * Water count per day picture
     */
    fun waterCat(waterCountNorma: Int, waterCount: Int) {

        val percent: Float = (waterCount.toFloat() / waterCountNorma.toFloat()) * 100

        when (percent.toInt()) {
            0 -> {
                _mainFragmentDrinkCountView.value = Event(1)
            }
            in 1..33 -> {
                _mainFragmentDrinkCountView.value = Event(2)
            }
            in 34..59 -> {
                _mainFragmentDrinkCountView.value = Event(3)
            }
            in 60..85 -> {
                _mainFragmentDrinkCountView.value = Event(4)
            }
            else -> {
                _mainFragmentDrinkCountView.value = Event(5)
            }
        }
    }


    /**
     * Add drink.
     * First select count of water in the drink
     */
    fun addDrink() = viewModelScope.launch {
        val drink = drinkRepository.getDrink(preferencesRepository.getDrinkType())
        addWaterCount(drink)
    }

    @SuppressLint("DiscouragedApi")
    private fun addWaterCount(drinks: Drinks) = viewModelScope.launch {
        val calendar: Calendar = Calendar.getInstance()
        val context = getApplication<Application>().applicationContext

        val water = Water()
        water.countWater = (drinks.percent *  preferencesRepository.getDrinkCount().toDouble()) / 100.0
        water.countDrink = preferencesRepository.getDrinkCount().toDouble()
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

        waterRepository.addWaterCount(water)

        drinkAdded.value = Event(true)// update water count and water picture

        // update next notification
        AlarmReceiver.setAlarm(context)
    }

    /**
     * Water count per once
     */
    fun getWaterCountPerOnce() {
        _drinkCountPerOnce.value = Event(preferencesRepository.getDrinkCount())
    }

    ///////////////////////////////////////Notify Fragment//////////////////////////////////////////////////////////

    // NotifyFragment Drink count
    private val _notifyFragmentFreq = MutableLiveData<Event<String>>()
    val notifyFragmentFreq: LiveData<Event<String>>
        get() = _notifyFragmentFreq

    // NotifyFragment Period
    private val _notifyFragmentPeriod = MutableLiveData<Event<Bundle>>()
    val notifyFragmentPeriod: LiveData<Event<Bundle>>
        get() = _notifyFragmentPeriod

    // NotifyFragment Norma Over
    private val _notifyFragmentNorma = MutableLiveData<Event<Boolean>>()
    val notifyFragmentNorma: LiveData<Event<Boolean>>
        get() = _notifyFragmentNorma

    // NotifyFragment Notify On
    private val _notifyFragmentNotify = MutableLiveData<Event<Boolean>>()
    val notifyFragmentNotify: LiveData<Event<Boolean>>
        get() = _notifyFragmentNotify

    private fun getPeriod() {
        viewModelScope.launch {
            val user: User = userRepository.user(preferencesRepository.getUserId())!!

            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, user.wakeUpHour)
            calendar.set(Calendar.MINUTE, user.wakeUpMinute)

            val timeWakeUp: String = formatter.format(calendar.time)

            calendar.set(Calendar.HOUR_OF_DAY, user.bedHour)
            calendar.set(Calendar.MINUTE, user.bedMinute)
            val timeGoBed: String = formatter.format(calendar.time)

            val bundle = Bundle()
            bundle.putString("timeWakeUp", timeWakeUp)
            bundle.putString("timeGoBed", timeGoBed)

            _notifyFragmentPeriod.value = Event(bundle)
        }
    }

    fun getFreq() {
        val context = getApplication<Application>().applicationContext
        val hour = preferencesRepository.getWaterIntervalHour()
        val minute = preferencesRepository.getWaterIntervalMinute()
        var string = ""
        if (hour == 0) {
            string = "$minute" + context.getString(R.string.notify_freq_value_min)
        } else {
            string = "$hour" + context.getString(R.string.notify_freq_value_hour)
            string += " $minute" + context.getString(R.string.notify_freq_value_min)
        }

        _notifyFragmentFreq.value = Event(string)
    }

    fun saveWaterPeriod(wakeUpHour: Int, wakeUpMinute: Int, goBedHour: Int, goBedMinute: Int) {
        viewModelScope.launch {
            val user: User = userRepository.user(preferencesRepository.getUserId())!!
            user.wakeUpHour = wakeUpHour
            user.wakeUpMinute = wakeUpMinute
            user.bedHour = goBedHour
            user.bedMinute = goBedMinute


            userRepository.update(user)
        }
    }

    fun getParams() {
        _notifyFragmentNorma.value = Event(preferencesRepository.getNormaOver())
        getPeriod()
        getFreq()
        _notifyFragmentNotify.value = Event(preferencesRepository.notify())
    }

    fun saveOver(over: Boolean) {
        preferencesRepository.saveNormaOver(over)
    }

    fun saveNotifyOn(on: Boolean) {
        preferencesRepository.saveNotify(on)
    }

    fun getSound(): String {
        var string = preferencesRepository.getSound()
        if (string == null)
            string = "notification_sound"
        return string
    }

    ///////////////////////////////////////Settings Fragment//////////////////////////////////////////////////////////

    // NotifyFragment Drink count
    private val _settingsFragmentWaterDaily = MutableLiveData<Event<String>>()
    val settingsFragmentWaterDaily: LiveData<Event<String>>
        get() = _settingsFragmentWaterDaily

    // NotifyFragment Gender
    private val _settingsFragmentGender = MutableLiveData<Event<Int>>()
    val settingsFragmentGender: LiveData<Event<Int>>
        get() = _settingsFragmentGender

    // NotifyFragment Weight
    private val _settingsFragmentWeight = MutableLiveData<Event<Double>>()
    val settingsFragmentWeight: LiveData<Event<Double>>
        get() = _settingsFragmentWeight

    // NotifyFragment Hide Rate us
    private val _settingsFragmentHideRateUs = MutableLiveData<Event<Boolean>>()
    val settingsFragmentHideRateUs: LiveData<Event<Boolean>>
        get() = _settingsFragmentHideRateUs

    // NotifyFragment Water Personal
    private val _settingsFragmentWater = MutableLiveData<Event<Boolean>>()
    val settingsFragmentWater: LiveData<Event<Boolean>>
        get() = _settingsFragmentWater

    fun getWaterDaily() {
        _settingsFragmentWaterDaily.value = Event(preferencesRepository.getDrinkCountPerDay().toString())
    }

    fun getGenderFragment() {
        viewModelScope.launch {
            val user: User = userRepository.user(preferencesRepository.getUserId())!!
            _settingsFragmentGender.value = Event(user.gender)
        }
    }

    /**
     * Get current weight
     */
    fun getWeightFragment() {
        viewModelScope.launch {
            val weight: Weight = weightRepository.getLastWeightMaybe()!!
            _settingsFragmentWeight.value = Event(weight.weight)
            getWater(weight.weight)
        }
    }

    /**
     * calculate water count
     */
    private fun getWater(weight: Double) {
        _settingsFragmentWater.value = Event(preferencesRepository.getDrinkCountPersonal())
        if (preferencesRepository.getDrinkCountPersonal()) {// user water count
            _settingsFragmentWaterDaily.value = Event(preferencesRepository.getDrinkCountPerDay().toString())
        } else {// calculate water count from weight
            val format: NumberFormat = DecimalFormat("#")
            _settingsFragmentWaterDaily.value = Event(format.format(weight * 30))
        }

    }

    fun saveRate() {
        preferencesRepository.saveRateUs()
        _settingsFragmentHideRateUs.value = Event(true)
    }

    fun fillValues() {
        getGenderFragment()// gender
        getWeightFragment()// weight

        // rate us
        if (preferencesRepository.getRateUs()) {
            _settingsFragmentHideRateUs.value = Event(true)
        }
    }

    fun saveWaterCountPersonal(personal: Boolean) {
        preferencesRepository.saveDrinkCountPersonal(personal)
    }

    ///////////////////////////////////////Water Fragment//////////////////////////////////////////////////////////

    private var currentDay: Long = 0L
    private var dateSystemFormat: DateFormat? = android.text.format.DateFormat.getDateFormat(getApplication<Application>().applicationContext) // system date format
    private val dateShortSystemFormat = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "ddMM") // system date format
    private var barIndex = 0f
    private val dateValues: ArrayList<String> = ArrayList()

    // WaterFragment Water Log
    private val _waterFragmentLog = MutableLiveData<Event<List<Water>>>()
    val waterFragmentLog: LiveData<Event<List<Water>>>
        get() = _waterFragmentLog

    // WaterFragment Water Graph
    private val _waterFragmentGraph = MutableLiveData<Event<Bundle>>()
    val waterFragmentGraph: LiveData<Event<Bundle>>
        get() = _waterFragmentGraph

    // WaterFragment Water Day
    private val _waterFragmentWaterDay = MutableLiveData<Event<String>>()
    val waterFragmentWaterDay: LiveData<Event<String>>
        get() = _waterFragmentWaterDay

    // WaterFragment Water Day
    private val _waterFragmentShowWater = MutableLiveData<Event<Boolean>>()
    val waterFragmentShowWater: LiveData<Event<Boolean>>
        get() = _waterFragmentShowWater

    fun setToday() {
        val today = Calendar.getInstance()
        currentDay = today.timeInMillis
    }

    /**
     * Set Current day
     */
    fun setCurrentDay() {
        val date = Date(currentDay)
        val todayDate: String? = dateSystemFormat?.format(date)
        _waterFragmentWaterDay.value = Event(todayDate!!)
    }

    /**
     * Add drink
     */
    fun addDrinkFragment() {
        // get favorite drink type
        viewModelScope.launch {
            val drink: Drinks = drinkRepository.getDrink(preferencesRepository.getDrinkType())
            addWaterCountFragment(drink)
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun addWaterCountFragment(drinks: Drinks) {
        val calendar: Calendar = Calendar.getInstance()
        val context = getApplication<Application>().applicationContext

        val water = Water()
        water.countWater = (drinks.percent *  preferencesRepository.getDrinkCount().toDouble()) / 100.0
        water.countDrink = preferencesRepository.getDrinkCount().toDouble()
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
        viewModelScope.launch {
            waterRepository.addWaterCount(water)
            getWaters()// get new lists water
            getGraph()// recreate graph
        }
    }

    fun getWaters() {
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

        viewModelScope.launch {
            val waterList: List<Water> = waterRepository.getWaterCountToday(dayBegin, dayEnd)!!
            _waterFragmentLog.value = Event(waterList)
        }
    }

    /**
     * Water graph
     * Show graph for 7 days
     */
    fun getGraph() {

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

        viewModelScope.launch {
            val water: List<Water> = waterRepository.getAllWater(dayBegin, dayEnd)!!
            createGraph(water, dayBegin, dayEnd)
        }
    }

    private fun createGraph(water: List<Water>, dayBegin: Long, dayEnd: Long) {
        barIndex = 0f
        dateValues.clear()

        val values: ArrayList<BarEntry> = ArrayList()
        val dateFormat = SimpleDateFormat(dateShortSystemFormat, Locale.getDefault())

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
        val bundle = Bundle()
        bundle.putParcelableArrayList("values", values)
        bundle.putStringArrayList("dateValues", dateValues)
        _waterFragmentGraph.value = Event(bundle)
    }

    /**
     * Fill empty dates
     * TODO check year
     */
    private fun fillDates(startDay: Long, endDay: Long): ArrayList<BarEntry> {
        val values: ArrayList<BarEntry> = ArrayList()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat(dateShortSystemFormat, Locale.getDefault())

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

    /**
     * Set new current day after graph clicked
     * TODO add check new/old year
     */
    fun setNewCurrentDay(day: String) {
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
                _waterFragmentShowWater.value = Event(false)
            } else {
                _waterFragmentShowWater.value = Event(true)
            }

            setCurrentDay() // set current day to view
            getWaters() // show daily water log
        } catch (ex: ParseException) {
            ex.printStackTrace()
        }
    }

    ///////////////////////////////////////Weight Fragment//////////////////////////////////////////////////////////

    // WeightFragment Weight Log
    private val _weightFragmentLog = MutableLiveData<Event<List<Weight>>>()
    val weightFragmentLog: LiveData<Event<List<Weight>>>
        get() = _weightFragmentLog

    // WeightFragment Current Weight
    private val _weightFragmentCurrent = MutableLiveData<Event<Weight>>()
    val weightFragmentCurrent: LiveData<Event<Weight>>
        get() = _weightFragmentCurrent

    // WeightFragment Graph
    private val _weightFragmentGraph = MutableLiveData<Event<ArrayList<Entry>>>()
    val weightFragmentGraph: LiveData<Event<ArrayList<Entry>>>
        get() = _weightFragmentGraph

    fun getWeights() {
        viewModelScope.launch {
            _weightFragmentLog.value = Event(weightRepository.getWeightsPeriod()!!)
        }
    }

    fun getCurrentWeightFragment() {
        viewModelScope.launch {
            _weightFragmentCurrent.value = Event(weightRepository.getLastWeightSingle()!!)
        }
    }

    /**
     * Graph for whole period
     */
    fun getGraphFragment() {
        viewModelScope.launch {
            val weight: List<Weight> = weightRepository.getWeightsGraph()!!
            createWeightData(weight)
        }
    }

    private fun createWeightData(weight: List<Weight>) {
        val values: ArrayList<Entry> = ArrayList()

        for (i in weight) {
            values.add(
                Entry(i.createAt.toFloat(), i.weight.toFloat())
            )
        }
        _weightFragmentGraph.value = Event(values)
    }

    ///////////////////////// Dialog Drink ////////////////////////////////////

    // Drink count
    private val dialogDrinkCount = MutableLiveData<Event<Int>>()
    val drinkCount: LiveData<Event<Int>>
        get() = dialogDrinkCount

    // Drink Error
    private val dialogDrinkError = MutableLiveData<Event<Boolean>>()
    val drinkError: LiveData<Event<Boolean>>
        get() = dialogDrinkError

    // Close dialog
    private val dialogDrinkClose = MutableLiveData<Event<Int>>()
    val drinkClose: LiveData<Event<Int>>
        get() = dialogDrinkClose

    fun getCurrentDrinkCount() {
        dialogDrinkCount.value = Event(preferencesRepository.getDrinkCount())
    }

    fun saveDrink(water: String) {
        val waterInt: Int = water.toInt()
        if (waterInt >= 10) {
            preferencesRepository.saveDrinkCount(waterInt)

            dialogDrinkClose.value = Event(waterInt)
        } else {
            dialogDrinkError.value = Event(true)
        }
    }

    ///////////////////////// Dialog Drink Select ////////////////////////////////////

    // Drinks
    private val dialogDrinkSelectList = MutableLiveData<Event<ArrayList<DrinksView>>>()
    val drinkSelectList: LiveData<Event<ArrayList<DrinksView>>>
        get() = dialogDrinkSelectList

    // Drink selected
    private val dialogDrinkSelected = MutableLiveData<Event<Boolean>>()
    val drinkSelected: LiveData<Event<Boolean>>
        get() = dialogDrinkSelected

    fun getDrinks() {
        viewModelScope.launch {
            createDrinksList(drinkRepository.getDrinks())
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun createDrinksList(drinks: List<Drinks>) {

        val drinksView: ArrayList<DrinksView> = ArrayList()
        val selectedDrink = preferencesRepository.getDrinkType()
        val context = getApplication<Application>().applicationContext

        for (drink in drinks) {
            if (drink.system)
                drink.name = context.getString(context.resources.getIdentifier(drink.name, "string", context.packageName))
            drinksView.add(DrinksView(drink, selectedDrink == drink.id))
        }

        dialogDrinkSelectList.value = Event(drinksView)
    }

    fun saveDrink(id: Long) {
        preferencesRepository.saveDrinkType(id) // save
        dialogDrinkSelected.value = Event(true) // show saved
    }

    ///////////////////////// Dialog Gender ////////////////////////////////////

    // Gender
    private val dialogGenderUser = MutableLiveData<Event<User>>()
    val genderUser: LiveData<Event<User>>
        get() = dialogGenderUser

    // Gender selected
    private val dialogGenderSelected = MutableLiveData<Event<Boolean>>()
    val genderSelected: LiveData<Event<Boolean>>
        get() = dialogGenderSelected

    fun getGender() {
        viewModelScope.launch {
            dialogGenderUser.value = Event(userRepository.user(preferencesRepository.getUserId())!!)
        }
    }

    fun saveGender(user: User) {
        viewModelScope.launch {
            userRepository.update(user)
            dialogGenderSelected.value = Event(true)
        }
    }

    ///////////////////////// Dialog Water ////////////////////////////////////

    // Water current
    private val dialogWaterCurrent = MutableLiveData<Event<Int>>()
    val waterCurrent: LiveData<Event<Int>>
        get() = dialogWaterCurrent

    // Water selected
    private val dialogWaterClose = MutableLiveData<Event<Boolean>>()
    val waterClose: LiveData<Event<Boolean>>
        get() = dialogWaterClose

    // Error
    private val dialogWaterError = MutableLiveData<Event<Boolean>>()
    val waterError: LiveData<Event<Boolean>>
        get() = dialogWaterError

    fun getCurrentWater() {
        dialogWaterCurrent.value = Event(preferencesRepository.getDrinkCountPerDay())
    }

    fun saveWater(water: String) {
        if (water != "") {
            val waterInt: Int = water.toInt()
            if (waterInt > 0) {
                preferencesRepository.saveDrinkCountPerDay(waterInt)
                dialogWaterClose.value = Event(true)
            } else {
                dialogWaterError.value = Event(true)
            }
        } else {
            dialogWaterError.value = Event(true)
        }
    }

    ///////////////////////// Dialog Water Interval ////////////////////////////////////

    // Interval Hour
    private val dialogWaterIntervalHour = MutableLiveData<Event<Int>>()
    val waterIntervalHour: LiveData<Event<Int>>
        get() = dialogWaterIntervalHour

    // Interval Minute
    private val dialogWaterIntervalMinute = MutableLiveData<Event<Int>>()
    val waterIntervalMinute: LiveData<Event<Int>>
        get() = dialogWaterIntervalMinute

    // Error
    private val dialogWaterIntervalError = MutableLiveData<Event<Boolean>>()
    val waterIntervalError: LiveData<Event<Boolean>>
        get() = dialogWaterIntervalError

    // Close dialog
    private val dialogWaterIntervalClose = MutableLiveData<Event<Boolean>>()
    val waterIntervalClose: LiveData<Event<Boolean>>
        get() = dialogWaterIntervalClose

    fun getIntervalHour() {
        dialogWaterIntervalHour.value = Event(preferencesRepository.getWaterIntervalHour())
    }

    fun getIntervalMinute() {
        dialogWaterIntervalMinute.value = Event(preferencesRepository.getWaterIntervalMinute())
    }

    fun saveWaterInterval(selectedHour: String, selectedMinute: String) {
        var errors = false

        // interval must be more 15 minutes
        if (selectedHour == "0") {
            if (selectedMinute < "15") {
                errors = true
                dialogWaterIntervalError.value = Event(true)
            }
        }

        if (!errors) {
            // save period
            preferencesRepository.saveWaterIntervalHour(selectedHour.toInt())
            preferencesRepository.saveWaterIntervalMinute(selectedMinute.toInt())

            dialogWaterIntervalClose.value = Event(true)
        }
    }

    ///////////////////////// Dialog Water Period ////////////////////////////////////

    // Set data
    private val dialogWaterPeriod = MutableLiveData<Event<User>>()
    val waterPeriod: LiveData<Event<User>>
        get() = dialogWaterPeriod

    // Error
    private val dialogWaterPeriodError = MutableLiveData<Event<Int>>()
    val waterPeriodError: LiveData<Event<Int>>
        get() = dialogWaterPeriodError

    // Close dialog
    private val dialogWaterPeriodClose = MutableLiveData<Event<Bundle>>()
    val waterPeriodClose: LiveData<Event<Bundle>>
        get() = dialogWaterPeriodClose

    fun getWaterPeriod() {
        viewModelScope.launch {
            dialogWaterPeriod.value = Event(userRepository.user(preferencesRepository.getUserId())!!)
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun saveWaterPeriod(user: User) {
        var errors = false

        val simpleDateFormat = SimpleDateFormat("hh:mm")

        val date1 = simpleDateFormat.parse("${user.wakeUpHour}:${user.wakeUpMinute}")
        val date2 = simpleDateFormat.parse("${user.bedHour}:${user.bedMinute}")

        val difference: Long = (date2?.time ?: 0) - (date1?.time ?: 0)
        val days = (difference / (1000 * 60 * 60 * 24)).toInt()
        val hours = ((difference - 1000 * 60 * 60 * 24 * days) / (1000 * 60 * 60)).toInt()

        if (hours < 0) {
            errors = true
            dialogWaterPeriodError.value = Event(0)
        } else if (hours < 3) {
            errors = true
            dialogWaterPeriodError.value = Event(1)
        }

        if (!errors) {
            // save user
            viewModelScope.launch {
                userRepository.update(user)
            }

            val bundle = Bundle()
            bundle.putInt("wakeup_time_hour", user.wakeUpHour)
            bundle.putInt("wakeup_time_minute", user.wakeUpMinute)
            bundle.putInt("go_bed_time_hour", user.bedHour)
            bundle.putInt("go_bed_time_minute", user.bedMinute)

            dialogWaterPeriodClose.value = Event(bundle)
        }
    }

    ///////////////////////// Dialog Weight ////////////////////////////////////

    // Set data
    private val dialogWeightData = MutableLiveData<Event<Weight>>()
    val weightData: LiveData<Event<Weight>>
        get() = dialogWeightData

    // Error dialog
    private val dialogWeightErrors = MutableLiveData<Event<Int>>()
    val weightErrors: LiveData<Event<Int>>
        get() = dialogWeightErrors

    // Close dialog
    private val dialogWeightClose = MutableLiveData<Event<Boolean>>()
    val weightClose: LiveData<Event<Boolean>>
        get() = dialogWeightClose

    fun getCurrentWeight() {
        viewModelScope.launch {
            dialogWeightData.value = Event(weightRepository.getLastWeightMaybe()!!)
        }
    }

    fun saveWeight(weight: String) {
        var errors = false
        var weightDouble = 0.0
        if (weight.isEmpty()) {
            errors = true
            dialogWeightErrors.value = Event(0)
        } else {
            weightDouble = weight.toDouble()
            if (weightDouble < 30 || weightDouble > 300) {// weight between 10 ~ 300
                errors = true
                dialogWeightErrors.value = Event(1)
            }
        }

        if (!errors) {
            viewModelScope.launch {
                val currentWeight: Weight = weightRepository.getLastWeightMaybe()!!
                saveWeight2(weightDouble, currentWeight)
            }
        }
    }

    private suspend fun saveWeight2(weight: Double, currentWeight: Weight) {

        val oldFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = currentWeight.createAt

        if (oldFormatter.format(Date()) == oldFormatter.format(calendar.time)) {// update today weight
            currentWeight.weight = weight

            weightRepository.update(currentWeight)
        } else {// new weight
            val newWeight = Weight()
            val newCalendar: Calendar = Calendar.getInstance()
            newWeight.createAt = newCalendar.timeInMillis
            newWeight.weight = weight

            weightRepository.insert(newWeight)
        }

        dialogWeightClose.value = Event(true)
    }
}