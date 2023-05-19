package com.iarigo.water.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Table with drinks
 * Drink name - Percent of water - System|User
 */
@Entity(indices = [Index(value = ["_id"], unique = true)])
data class Drinks (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0L,
    var name: String = "Water",
    var percent: Int = 100,
    var system: Boolean = false
)