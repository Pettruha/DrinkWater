package com.iarigo.water.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Water count per day
 */
@Entity(indices = [Index(value = ["_id"], unique = true)])
data class Water (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0L,
    @ColumnInfo(name = "count_water")
    var countWater: Double = 0.0,
    @ColumnInfo(name = "count_drink")
    var countDrink: Double = 0.0,
    @ColumnInfo(name = "drink_name")
    var drinkName: String = "",
    @ColumnInfo(name = "create_at")
    var createAt: Long = 0L,
    @ColumnInfo(name = "day_at")
    var dayAt: Long = 0L
)