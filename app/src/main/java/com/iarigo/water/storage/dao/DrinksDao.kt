package com.iarigo.water.storage.dao

import androidx.room.*
import com.iarigo.water.storage.entity.Drinks
import com.iarigo.water.storage.entity.User
import com.iarigo.water.storage.entity.Weight
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface DrinksDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(drinksList: MutableList<Drinks>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(drink: Drinks): Single<Long>

    // напитки
    @Query("SELECT * FROM drinks ORDER BY _id")
    fun getDrinks(): Maybe<List<Drinks>>

    // напиток
    @Query("SELECT * FROM drinks WHERE _id = :id")
    fun getDrink(id: Long): Maybe<Drinks>

    @Update
    fun update(drink: Drinks)

    @Delete
    fun delete(drink: Drinks)
}