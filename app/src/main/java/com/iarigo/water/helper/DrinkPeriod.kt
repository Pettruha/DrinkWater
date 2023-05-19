package com.iarigo.water.helper

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.iarigo.water.repository.PreferencesRepository
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.User
import com.iarigo.water.storage.entity.Water
import com.iarigo.water.storage.entity.Weight
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Calculate time to next drink
 * Return milliseconds from current time to drink time
 */

class DrinkPeriod {
    companion object {

        fun drinkTime(context: Context): Long {
            val preferencesRepository = PreferencesRepository(context as Application)
            var period: Long = 0L
            val user = getUser(context, preferencesRepository.getUserId())

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

        private fun getUser(context: Context, userId: Long): User {
            return runBlocking(CoroutineScope(Dispatchers.IO).coroutineContext) {
                return@runBlocking user(context, userId)!!
            }
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
        private suspend fun user(context: Context, userId: Long): User? {
            val appDatabase: AppDatabase? = AppDatabase.getAppDataBase(context)
            return appDatabase?.withTransaction { appDatabase.userDao().getUser(userId) }
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
            val waters: List<Water> = getWaterList(context, dayBegin, dayEnd)

            if (waters.isNotEmpty()) {
                for (one in waters) {
                    water += one.countWater.toInt()
                }
            }

            return water
        }

        private fun getWaterList(context: Context, dayBegin: Long, dayEnd: Long): List<Water> {
            return runBlocking(CoroutineScope(Dispatchers.IO).coroutineContext) {
                return@runBlocking getWaters(context, dayBegin, dayEnd)
            }
        }

        private suspend fun getWaters(context: Context, dayBegin: Long, dayEnd: Long) = withContext(Dispatchers.IO) {
            val appDatabase: AppDatabase? = AppDatabase.getAppDataBase(context)
            appDatabase?.waterDao()?.getAllWater(dayBegin, dayEnd)!!
        }

        /**
         * Current weight
         */
        private fun currentWater(context: Context): Int {
            return runBlocking(CoroutineScope(Dispatchers.IO).coroutineContext) {
                var waterCountNorma = 1800
                val weight: Weight = getWeight(context)

                if (weight.id != 0L) {
                    waterCountNorma = (weight.weight * 30).toInt()
                }

                return@runBlocking waterCountNorma
            }
        }

        private suspend fun getWeight(context: Context) = withContext(Dispatchers.IO) {
            val appDatabase: AppDatabase? = AppDatabase.getAppDataBase(context)
            appDatabase?.weightDao()?.getLastWeight()!!
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