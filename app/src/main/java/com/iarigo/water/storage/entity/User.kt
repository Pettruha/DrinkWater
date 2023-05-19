package com.iarigo.water.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * User:
 * - Gender
 * - Water count per day
 * - Wake up time
 * - Time to bed
 */
@Entity(indices = [Index(value = ["_id"], unique = true)])
data class User (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0L,
    var gender: Int = 1, // 0 - woman; 1 - man
    @ColumnInfo(name = "wake_up_hour")
    var wakeUpHour: Int = 6, // wake up hour
    @ColumnInfo(name = "wake_up_minute")
    var wakeUpMinute: Int = 6, // wake up minute
    @ColumnInfo(name = "bed_hour")
    var bedHour: Int = 6, // time to bed hour
    @ColumnInfo(name = "bed_minute")
    var bedMinute: Int = 6, // time to bed minute
)