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
    abstract fun userDao(): UserDao // поль-ль
    abstract fun drinksDao(): DrinksDao // напитки
    abstract fun waterDao(): WaterDao // приём воды
    abstract fun weightDao(): WeightDao // взвешивания

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
         * Заполняем начальными значениями таблицу с напитками
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
         * Начальные данные напитков и процент воды, который в них содержится
         *
         * Первый параметр - название
         * Второй параметр - процент воды
         * Третий - система (1)/поль-ля (0)
         *
         */
        private var initCoupons = arrayOf(
            "Вода|100|1",
            "Газированная вода|100|1",
            "Эспрессо|100|1",
            "Американо|100|1",
            "Капучино|92|1",
            "Латте|90|1",
            "Макаито|99|1",
            "Латте макиато|89|1",
            "Мокко|90|1",
            "Флэт Уайт|93|1",
            "Черный чай|100|1",
            "Зеленый чай|100|1",
            "Кола|89|1",
            "Диетическая кола|100|1",
            "Цельное молоко|88|1",
            "Обезжиренное молоко|88|1",
            "Спортивный напиток|96|1",
            "Сок|89|1"
        )

        fun destroyDataBase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}