package com.iarigo.water.storage.dao

import androidx.room.*
import com.iarigo.water.storage.entity.User
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface UserDao {
    // выбираем поль-ля
    @Query("SELECT * FROM user WHERE _id = :id")
    fun getUser(id: Long): Single<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User): Single<Long>

    @Update
    fun update(user: User): Maybe<Int>

    @Delete
    fun delete(user: User)
}