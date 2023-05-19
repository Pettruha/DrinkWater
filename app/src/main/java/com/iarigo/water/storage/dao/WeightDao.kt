package com.iarigo.water.storage.dao

import androidx.room.*
import com.iarigo.water.storage.entity.Weight
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface WeightDao {
    // last weight
    @Query("SELECT * FROM weight ORDER BY create_at DESC LIMIT 1")
    suspend fun getLastWeight(): Weight

    // last weight
    @Query("SELECT * FROM weight ORDER BY create_at DESC LIMIT 1")
    suspend fun getLastWeightSingle(): Weight

    // last weight
    @Query("SELECT * FROM weight ORDER BY create_at DESC LIMIT 1")
    suspend fun getLastWeightMaybe(): Weight

    // all weights
    @Query("SELECT * FROM weight ORDER BY create_at DESC")
    suspend fun getWeightsPeriod(): List<Weight>

    // weights for graph
    @Query("SELECT * FROM weight ORDER BY create_at ASC")
    suspend fun getWeightsGraph(): List<Weight>

    // min date
    @Query("SELECT * FROM weight ORDER BY create_at ASC LIMIT 1")
    fun getMinDate(): Maybe<Weight>

    // max date
    @Query("SELECT * FROM weight ORDER BY create_at DESC LIMIT 1")
    fun getMaxDate(): Maybe<Weight>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFirstTime(weight: Weight): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weight: Weight): Long

    @Update
    suspend fun update(weight: Weight): Int

    @Delete
    fun delete(weight: Weight)
}