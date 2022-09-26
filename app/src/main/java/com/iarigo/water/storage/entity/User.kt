package com.iarigo.water.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Пользователь
 * Пол
 * Кол-во воды в сутки
 * Время подъема
 * Время отбоя
 */
@Entity(indices = [Index(value = ["_id"], unique = true)])
data class User (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0L,
    var sex: Int = 1, // 0 - жен; 1 - муж
    @ColumnInfo(name = "wake_up_hour")
    var wakeUpHour: Int = 6, // час подъема
    @ColumnInfo(name = "wake_up_minute")
    var wakeUpMinute: Int = 6, // минута подъема
    @ColumnInfo(name = "bed_hour")
    var bedHour: Int = 6, // час отбоя
    @ColumnInfo(name = "bed_minute")
    var bedMinute: Int = 6, // минута отбоя
) {}