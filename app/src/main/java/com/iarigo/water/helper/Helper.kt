package com.iarigo.water.helper

import android.content.SharedPreferences
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class Helper {
    companion object {

        const val FIRST_VISITED = "hasVisited" // самый первый запуск
        const val USER_ID = "userId"
        const val NOTIFY_WATER_OVER = "notify_water_over"
        const val NOTIFY_ON = "notify_on"
        const val NOTIFY_FREQ_MINUTE = "notify_freq_minute"
        const val NOTIFY_FREQ_HOUR = "notify_freq_hour"
        const val NOTIFY_SOUND = "notify_sound"
        const val WATER_COUNT_PER_DAY = "water_count"
        const val WATER_COUNT_PERSONAL = "water_count_personal"
        const val WATER_DRINK_SELECTED = "1"
        const val WATER_COUNT = "200"
        const val RATE = "rate"

        /**
         * Дата из String UTC в Date
         * @param StrDate
         * @return
         */
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        fun stringDateToDate(StrDate: String): Date {
            var dateToReturn = Date()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")

            try {
                val dateInUTC = dateFormat.parse(StrDate)
                dateFormat.timeZone = TimeZone.getDefault()
                val stringLocal = dateFormat.format(Objects.requireNonNull(dateInUTC))
                dateToReturn = dateFormat.parse(stringLocal)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return dateToReturn
        }

        /**
         * Конвертируем строковую дату в Date
         */
        fun stringDateToMilliSeconds(date: String): Long {
            val calendar = Calendar.getInstance() // для инициализации даты по умолчанию
            val dateFormat = stringDateToDate(date) // дата Date в формате UTC
            try {
                calendar.time = dateFormat
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return calendar.timeInMillis
        }
    }
}