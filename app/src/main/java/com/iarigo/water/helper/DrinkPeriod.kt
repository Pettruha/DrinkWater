package com.iarigo.water.helper

import android.app.Application
import android.content.Context
import com.iarigo.water.repository.PreferencesRepository
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.User
import java.util.*

/**
 * Calculate time to next drink
 * Return milliseconds from current time to drink time
 */

class DrinkPeriod {
    companion object {
        fun drinkTime(context: Context): Long {
            val preferencesRepository: PreferencesRepository = PreferencesRepository(context as Application)
            var period = 0L

            val user = user(context, preferencesRepository.getUserId())

            if (!waterLimit(preferencesRepository, context)) {// notifications turn on after limit per day

                // next time to drink
                val next: Calendar = Calendar.getInstance()
                next.add(
                    Calendar.HOUR_OF_DAY,
                    preferencesRepository.getWaterIntervalHour()
                ) // add drink interval hours
                next.add(
                    Calendar.MINUTE,
                    preferencesRepository.getWaterIntervalMinute()
                ) // add drink interval minutes

                // check if next time hits into user water time
                period = if (compareDays(next.timeInMillis, user)) {
                    next.timeInMillis
                } else {// next day
                    nextDay(user)
                }
            } else {// next day
                period = nextDay(user)
            }

            return period
        }

        /**
         * Next day
         */
        private fun nextDay(user: User): Long {
            val calendar = Calendar.getInstance()

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, user.wakeUpHour)
            calendar.set(Calendar.MINUTE, user.wakeUpMinute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            return calendar.timeInMillis
        }

        /**
         * User info
         */
        private fun user(context: Context, userId: Long): User {
            val appDatabase: AppDatabase? = AppDatabase.getAppDataBase(context)

            return appDatabase?.userDao()?.getUser(userId)?.blockingGet()!!
        }

        /**
         * Check water limit reached today
         * or
         * turn On notification all day (time period)
         */
        private fun waterLimit(preferencesRepository: PreferencesRepository, context: Context): Boolean {
            val limit = if (preferencesRepository.getNormaOver()) {// notification all day
                false
            } else {// calculate count water drunk
                val waterNorma = waterNorma(preferencesRepository, context)
                val waterToday = waterToday(context)
                waterNorma < waterToday
            }
            return limit
        }

        /**
         * Count water per day
         */
        private fun waterNorma(preferencesRepository: PreferencesRepository, context: Context): Int {
            return if (!preferencesRepository.getDrinkCountPersonal()) {// calculate water count from weight
                currentWater(context)
            } else {// user's water count
                preferencesRepository.getDrinkCountPerDay()
            }
        }

        /**
         * Count water drunk user today
         */
        private fun waterToday(context: Context): Int {

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

            val appDatabase: AppDatabase? = AppDatabase.getAppDataBase(context)

            var water = 0
            val waters = appDatabase?.waterDao()?.getAllWater(dayBegin, dayEnd)?.blockingGet()
            if (waters != null) {
                for (one in waters) {
                    water += one.countWater.toInt()
                }
            }

            return water
        }

        /**
         * Current weight
         */
        private fun currentWater(context: Context): Int {
            val appDatabase: AppDatabase? = AppDatabase.getAppDataBase(context)
            var waterCountNorma = 1800
            val weight = appDatabase?.weightDao()?.getLastWeight()
            if (weight != null) {
                waterCountNorma = (weight.weight * 30).toInt()
            }
            return waterCountNorma
        }

        /**
         * compare time to bed and next water time
         */
        private fun compareDays(next: Long, user: User): Boolean {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, user.bedHour)
            calendar.set(Calendar.MINUTE, user.bedMinute)

            return next <= calendar.timeInMillis
        }
    }
}