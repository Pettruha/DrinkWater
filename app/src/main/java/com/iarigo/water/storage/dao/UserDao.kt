package com.iarigo.water.storage.dao

import androidx.room.*
import com.iarigo.water.storage.entity.User

@Dao
interface UserDao {
    // user
    @Query("SELECT * FROM user WHERE _id = :id")
    suspend fun getUser(id: Long): User

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User): Int

    @Delete
    suspend fun delete(user: User)
}