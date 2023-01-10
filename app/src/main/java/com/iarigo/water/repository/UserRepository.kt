package com.iarigo.water.repository

import android.app.Application
import com.iarigo.water.storage.dao.UserDao
import com.iarigo.water.storage.database.AppDatabase
import com.iarigo.water.storage.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class UserRepository(application: Application) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private var userDao: UserDao?

    init {
        val db = AppDatabase.getAppDataBase(application)
        userDao = db?.userDao()
    }

    // save User
    suspend fun insert(user: User) = withContext(Dispatchers.IO) {
        userDao?.insert(user)
    }


}