package com.iarigo.water.storage.dao

import androidx.room.*
import com.iarigo.water.storage.entity.User
import com.iarigo.water.storage.entity.Weight
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface WeightDao {
    // последнее взвешивание
    @Query("SELECT * FROM weight ORDER BY create_at DESC LIMIT 1")
    fun getLastWeight(): Maybe<Weight>

    // взвешивания
    @Query("SELECT * FROM weight ORDER BY create_at DESC")
    fun getWeightsPeriod(): Maybe<List<Weight>>

    // взвешивания для графика
    @Query("SELECT * FROM weight ORDER BY create_at ASC")
    fun getWeightsGraph(): Maybe<List<Weight>>

    // мин дата
    @Query("SELECT * FROM weight ORDER BY create_at ASC LIMIT 1")
    fun getMinDate(): Maybe<Weight>

    // макс дата
    @Query("SELECT * FROM weight ORDER BY create_at DESC LIMIT 1")
    fun getMaxDate(): Maybe<Weight>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(weight: Weight): Single<Long>

    @Update
    fun update(weight: Weight): Single<Int>

    @Delete
    fun delete(weight: Weight)
}