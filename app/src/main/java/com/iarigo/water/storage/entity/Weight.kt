package com.iarigo.water.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * User weights
 *
 */
@Entity(indices = [Index(value = ["_id"], unique = true)])
data class Weight (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0L,
    var weight: Double = 0.0,
    @ColumnInfo(name = "create_at")
    var createAt: Long = 0L
) {}