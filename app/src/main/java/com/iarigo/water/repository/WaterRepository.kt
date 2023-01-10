package com.iarigo.water.repository

import android.app.Application
import com.iarigo.water.storage.dao.WaterDao
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.Water
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class WaterRepository(application: Application) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private var waterDao: WaterDao?

    init {
        val db = AppDatabase.getAppDataBase(application)
        waterDao = db?.waterDao()
    }

    fun waterCount(start: Long, end: Long) = waterDao?.getWaterCount(start, end)

    suspend fun addWaterCount(water: Water) = withContext(Dispatchers.IO) {
        waterDao?.insert(water)
    }
}