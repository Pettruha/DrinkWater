package com.iarigo.water.storage.dao

import androidx.room.*
import com.iarigo.water.storage.entity.Water
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface WaterDao {
    // water count today
    @Query("SELECT SUM(count_water) FROM water WHERE create_at >= :day_begin AND create_at <= :day_end")
    fun getWaterCount(day_begin: Long, day_end: Long): Double

    // water log today
    @Query("SELECT * FROM water WHERE create_at >= :day_begin AND create_at <= :day_end ORDER BY create_at")
    fun getWaterCountToday(day_begin: Long, day_end: Long): Maybe<List<Water>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(water: Water): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWater(water: Water): Single<Long>

    // log water count per day
    @Query("SELECT '1' AS _id, SUM(count_water) AS count_water, '2' AS count_drink, '3' AS drink_name, CAST(create_at as date) AS create_at, CAST(day_at as date) AS day_at FROM water WHERE day_at >= :day_begin AND day_at <= :day_end GROUP BY CAST(day_at  as date) ORDER BY day_at")
    fun getAllWater(day_begin: Long, day_end: Long): Maybe<List<Water>>

    @Update
    fun update(water: Water)

    @Delete
    fun delete(water: Water)
}