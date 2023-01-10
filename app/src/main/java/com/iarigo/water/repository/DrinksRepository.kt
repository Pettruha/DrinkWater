package com.iarigo.water.repository

import android.app.Application
import com.iarigo.water.storage.dao.DrinksDao
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class DrinksRepository(application: Application) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private var drinksDao: DrinksDao?

    init {
        val db = AppDatabase.getAppDataBase(application)
        drinksDao = db?.drinksDao()
    }

    suspend fun getDrink(drink: Long) = withContext(Dispatchers.IO) {
        drinksDao!!.getDrink(drink)
    }

    fun getDrinks() = drinksDao!!.getDrinks()

    suspend fun insert() = withContext(Dispatchers.IO) {
        drinksDao!!.getDrinks()
    }
}