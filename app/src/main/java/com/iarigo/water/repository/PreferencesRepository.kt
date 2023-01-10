package com.iarigo.water.repository

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.iarigo.water.helper.Helper
import java.util.*

class PreferencesRepository (application: Application) {

    private var mSettings: SharedPreferences? =
        application.getSharedPreferences("water", Context.MODE_PRIVATE)

    /**
     * First app launch
     */
    fun firstLaunch(): Boolean {
        return mSettings!!.getBoolean(Helper.FIRST_VISITED, false)
    }

    /**
     * Save first app launch
     */
    fun saveFirstLaunch() {
        val e = mSettings!!.edit()
        e.putBoolean(Helper.FIRST_VISITED, true)
        // date first launch
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis
        e.putLong("startDate", today)
        e.apply()
    }

    /**
     * Save user info during first launch
     */
    fun saveUserInfo(userId: Long, waterCount: Int) {
        val e = mSettings!!.edit()
        e.putLong(Helper.USER_ID, userId)
        e.putBoolean(Helper.FIRST_VISITED, true)
        e.putInt(Helper.DRINK_COUNT_PER_DAY, waterCount)
        e.apply()
    }

    /**
     * User ID
     */
    fun getUserId(): Long {
        return mSettings!!.getLong(Helper.USER_ID, 1L)
    }

    /**
     * Get type of drink
     */
    fun getDrinkType(): Long {
        return mSettings!!.getLong(Helper.DRINK_SELECTED, 1L)
    }

    /**
     * Save type of drink
     */
    fun saveDrinkType(id: Long) {
        val e = mSettings!!.edit()
        e.putLong(Helper.DRINK_SELECTED, id)
        e.apply()
    }

    /**
     * Save drink count per once
     */
    fun saveDrinkCount(count: Int) {
        val e = mSettings!!.edit()
        e.putInt(Helper.DRINK_COUNT, count)
        e.apply()
    }

    /**
     * Get drink count per once
     */
    fun getDrinkCount(): Int {
        return mSettings!!.getInt(Helper.DRINK_COUNT, 200)
    }

    /**
     * Get drink count personal
     */
    fun getDrinkCountPersonal(): Boolean {
        return mSettings!!.getBoolean(Helper.DRINK_COUNT_PERSONAL, false)
    }

    /**
     * Save drink count personal
     */
    fun saveDrinkCountPersonal(personal: Boolean) {
        val e = mSettings!!.edit()
        e.putBoolean(Helper.DRINK_COUNT_PERSONAL, personal)
        e.apply()
    }

    /**
     * Get drink count per day
     */
    fun getDrinkCountPerDay(): Int {
        return mSettings!!.getInt(Helper.DRINK_COUNT_PER_DAY, 1800)
    }

    /**
     * Save drink count per day
     */
    fun saveDrinkCountPerDay(drink: Int) {
        val e = mSettings!!.edit()
        e.putInt(Helper.DRINK_COUNT_PER_DAY, drink)
        e.apply()
    }

    /**
     * Get water interval hour
     */
    fun getWaterIntervalHour(): Int {
        return mSettings!!.getInt(Helper.NOTIFY_FREQ_HOUR, 1)
    }

    /**
     * Save water interval hour
     */
    fun saveWaterIntervalHour(hour: Int) {
        val e = mSettings!!.edit()
        e.putInt(Helper.NOTIFY_FREQ_HOUR, hour)
        e.apply()
    }

    /**
     * Get water interval minute
     */
    fun getWaterIntervalMinute(): Int {
        return mSettings!!.getInt(Helper.NOTIFY_FREQ_MINUTE, 0)
    }

    /**
     * Save water interval minute
     */
    fun saveWaterIntervalMinute(minute: Int) {
        val e = mSettings!!.edit()
        e.putInt(Helper.NOTIFY_FREQ_MINUTE, minute)
        e.apply()
    }

    /**
     * Rate us
     */
    fun getRateUs(): Boolean {
        return mSettings!!.getBoolean(Helper.RATE, false)
    }

    /**
     * Save Rate Us
     */
    fun saveRateUs() {
        val e = mSettings!!.edit()
        e.putBoolean(Helper.RATE, true)
        e.apply()
    }

    /**
     * Get sound for notification
     * API < 26
     */
    fun getSound(): String? {
        return mSettings!!.getString(Helper.NOTIFY_SOUND, "notification_sound")
    }

    /**
     * Save sound for notification
     * API < 26
     */
    fun saveSound(sound: String) {
        val e = mSettings!!.edit()
        e.putString(Helper.NOTIFY_SOUND, sound)
        e.apply()
    }

    /**
     * Notification On when water norma per day done
     */
    fun getNormaOver(): Boolean {
        return mSettings!!.getBoolean(Helper.NOTIFY_WATER_OVER, false)
    }

    /**
     * Save notification On/Off when water norma per day done
     */
    fun saveNormaOver(over: Boolean) {
        val e = mSettings!!.edit()
        e.putBoolean(Helper.NOTIFY_WATER_OVER, over)
        e.apply()
    }

    /**
     * Notification On/Off
     */
    fun notify(): Boolean {
        return mSettings!!.getBoolean(Helper.NOTIFY_ON, true)
    }

    /**
     * Save notification On/Off
     */
    fun saveNotify(on: Boolean) {
        val e = mSettings!!.edit()
        e.putBoolean(Helper.NOTIFY_ON, on)
        e.apply()
    }
}