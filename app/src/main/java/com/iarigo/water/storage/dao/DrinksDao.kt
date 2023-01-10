package com.iarigo.water.storage.dao

import androidx.room.*
import com.iarigo.water.storage.entity.Drinks
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface DrinksDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(drinksList: MutableList<Drinks>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(drink: Drinks): Single<Long>

    // all drinks
    @Query("SELECT * FROM drinks ORDER BY _id")
    fun getDrinks(): Maybe<List<Drinks>>

    // one drink
    @Query("SELECT * FROM drinks WHERE _id = :id")
    suspend fun getDrink(id: Long): Drinks

    // one drink
    @Query("SELECT * FROM drinks WHERE _id = :id")
    fun get(id: Long): Maybe<Drinks>

    @Update
    fun update(drink: Drinks)

    @Delete
    fun delete(drink: Drinks)
}