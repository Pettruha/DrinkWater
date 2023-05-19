package com.iarigo.water.repository

import android.app.Application
import com.iarigo.water.storage.dao.WeightDao
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.Weight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class WeightRepository(application: Application) : CoroutineScope {
    // TODO снести?
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

    suspend fun insert(weight: Weight) {
        weightDao?.insert(weight)
    }

    suspend fun update(weight: Weight) = withContext(Dispatchers.IO) {
        weightDao?.update(weight)
    }

    suspend fun lastWeight() = withContext(Dispatchers.IO) {
        weightDao?.getLastWeight()
    }

    suspend fun getLastWeightMaybe() = withContext(Dispatchers.IO) {
        weightDao?.getLastWeightMaybe()
    }

    suspend fun getWeightsPeriod() = withContext(Dispatchers.IO) {
        weightDao?.getWeightsPeriod()
    }

    suspend fun getLastWeightSingle() =  withContext(Dispatchers.IO) {
        weightDao?.getLastWeightSingle()
    }

    suspend fun getWeightsGraph() = withContext(Dispatchers.IO) {
        weightDao?.getWeightsGraph()
    }
}