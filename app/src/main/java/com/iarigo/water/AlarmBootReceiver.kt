package com.iarigo.water

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Phone reboot. Start notifications.
 */
class AlarmBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            // if(intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED))
            // Intent serviceIntent = new Intent(context, AlarmService.class);
            // context.startService(serviceIntent);
            AlarmService.enqueueWork(context, Intent())
        }
    }
}