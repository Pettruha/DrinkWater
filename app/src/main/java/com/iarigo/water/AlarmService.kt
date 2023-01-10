package com.iarigo.water

import android.content.Context
import android.content.Intent
import androidx.annotation.NonNull

import androidx.core.app.JobIntentService

/**
 * Service Notifications
 */
class AlarmService : JobIntentService() {
    override fun onHandleWork(@NonNull intent: Intent) {
        AlarmReceiver.setAlarm(this)
    }

    companion object {
        private const val JOB_ID = 0x01
        fun enqueueWork(context: Context, work: Intent?) {
            enqueueWork(context, AlarmService::class.java, JOB_ID, work!!)
        }
    }
}