package com.iarigo.water.storage.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.iarigo.water.storage.dao.DrinksDao
import com.iarigo.water.storage.dao.UserDao
import com.iarigo.water.storage.dao.WaterDao
import com.iarigo.water.storage.dao.WeightDao
import com.iarigo.water.storage.entity.Drinks
import com.iarigo.water.storage.entity.User
import com.iarigo.water.storage.entity.Water
import com.iarigo.water.storage.entity.Weight
import java.util.concurrent.Executors

@Database(
    entities = [User::class, Drinks::class, Water::class, Weight::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao // user
    abstract fun drinksDao(): DrinksDao // drinks
    abstract fun waterDao(): WaterDao // water intakes
    abstract fun weightDao(): WeightDao // user weights

    companion object {
        @Volatile
        var INSTANCE: AppDatabase? = null

        fun getAppDataBase(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "water.db"
                    )
                        .addCallback(dbCallback)
                        .build()
                }
            }
            return INSTANCE
        }

        /**
         * Fill table drinks.
         */
        private var dbCallback: Callback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Executors.newSingleThreadScheduledExecutor()
                    .execute {
                        addDrinks()
                    }
            }
        }

        fun addDrinks(){
            val drinksList: ArrayList<Drinks> = ArrayList()

            for (value in initCoupons) {
                val ss = value.split("|").toTypedArray()
                drinksList.add(Drinks(0L, ss[0], ss[1].toInt(), ss[2] == "1"))
            }

            INSTANCE?.drinksDao()?.insertAll(drinksList)
        }

        /**
         * Some count of drinks.
         * Drink name and count of water in the drink
         *
         * First - drink name
         * Second - percent of water in the drink
         * Third - system (1)/user (0)
         */
        private var initCoupons = arrayOf(
            "water_type_1|100|1",
            "water_type_2|100|1",
            "water_type_3|100|1",
            "water_type_4|100|1",
            "water_type_5|92|1",
            "water_type_6|90|1",
            "water_type_7|99|1",
            "water_type_8|89|1",
            "water_type_9|90|1",
            "water_type_10|93|1",
            "water_type_11|100|1",
            "water_type_12|100|1",
            "water_type_13|89|1",
            "water_type_14|100|1",
            "water_type_15|88|1",
            "water_type_16|88|1",
            "water_type_17|96|1",
            "water_type_18|89|1"
        )

        fun destroyDataBase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}