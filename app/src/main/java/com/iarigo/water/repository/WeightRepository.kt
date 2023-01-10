package com.iarigo.water.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.iarigo.water.storage.dao.WeightDao
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.Weight
import io.reactivex.Maybe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class WeightRepository(application: Application) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private var weightDao: WeightDao?

    init {
        val db = AppDatabase.getAppDataBase(application)
        weightDao = db?.weightDao()
    }

    suspend fun insertFirstTime(weight: Weight) = withContext(Dispatchers.IO) {
        weightDao?.insertFirstTime(weight)
    }

    fun insert(weight: Weight) {
        weightDao?.insert(weight)
    }

    suspend fun lastWeight() = withContext(Dispatchers.IO) {
        weightDao?.getLastWeight()
    }
}