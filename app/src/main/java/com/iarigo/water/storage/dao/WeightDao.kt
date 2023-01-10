package com.iarigo.water.storage.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.iarigo.water.storage.entity.Weight
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface WeightDao {
    // last weight
    @Query("SELECT * FROM weight ORDER BY create_at DESC LIMIT 1")
    fun getLastWeight(): Weight

    // last weight
    @Query("SELECT * FROM weight ORDER BY create_at DESC LIMIT 1")
    fun getLastWeightSingle(): Maybe<Weight>

    // last weight
    @Query("SELECT * FROM weight ORDER BY create_at DESC LIMIT 1")
    fun getLastWeightMaybe(): Maybe<Weight>

    // all weights
    @Query("SELECT * FROM weight ORDER BY create_at DESC")
    fun getWeightsPeriod(): Maybe<List<Weight>>

    // weights for graph
    @Query("SELECT * FROM weight ORDER BY create_at ASC")
    fun getWeightsGraph(): Maybe<List<Weight>>

    // min date
    @Query("SELECT * FROM weight ORDER BY create_at ASC LIMIT 1")
    fun getMinDate(): Maybe<Weight>

    // max date
    @Query("SELECT * FROM weight ORDER BY create_at DESC LIMIT 1")
    fun getMaxDate(): Maybe<Weight>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFirstTime(weight: Weight): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(weight: Weight): Single<Long>

    @Update
    fun update(weight: Weight): Single<Int>

    @Delete
    fun delete(weight: Weight)
}