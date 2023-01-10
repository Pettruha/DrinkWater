package com.iarigo.water.ui.main

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.text.Editable
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.iarigo.water.AlarmReceiver
import com.iarigo.water.R
import com.iarigo.water.helper.Event
import com.iarigo.water.repository.*
import com.iarigo.water.storage.entity.Drinks
import com.iarigo.water.storage.entity.User
import com.iarigo.water.storage.entity.Water
import com.iarigo.water.storage.entity.Weight
import kotlinx.coroutines.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

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
                AlarmReceiver.registryNotificationChannel(getApplication<Application>())
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
        var water: Double = 0.0
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
        var errors : Boolean = false
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
            val user: User = User()
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
        val weight: Weight = Weight()
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

        val water: Water = Water()
        water.countWater = (drinks.percent *  preferencesRepository.getDrinkCount().toDouble()) / 100.0
        water.countDrink = preferencesRepository.getDrinkCount().toDouble()
        if (drinks.system) {// system name
            water.drinkName = context.getString(context.resources.getIdentifier(drinks.name, "string", context.packageName))
        } else water.drinkName = drinks.name
        water.createAt = calendar.timeInMillis

        // день
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 1)
        water.dayAt = calendar.timeInMillis

        waterRepository.addWaterCount(water)

        drinkAdded.value = Event(true)// update water count and water picture
    }

    /**
     * Water count per once
     */
    fun getWaterCountPerOnce() {
        _drinkCountPerOnce.value = Event(preferencesRepository.getDrinkCount())
    }
}