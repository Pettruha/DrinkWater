package com.iarigo.water

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.iarigo.water.helper.DrinkPeriod
import com.iarigo.water.repository.PreferencesRepository
import com.iarigo.water.ui.main.MainActivity
import java.text.SimpleDateFormat
import java.util.*

/**
 * set time for water notification
 */
class AlarmReceiver : BroadcastReceiver() {

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onReceive(context: Context, intent: Intent) {
        val preferencesRepository = PreferencesRepository(context.applicationContext as Application)

        // Get Activity which will be open on notification click
        val openIntent = Intent(context.applicationContext, MainActivity::class.java)
        val pi: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(context, 0, openIntent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(context, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notifyID = 2 // update previous notification
        val channel27ID: String = context.getString(R.string.water) //
        val channel27Name: String = context.getString(R.string.water_notification_text) // "Water notification";
        val channel27Description: String = context.getString(R.string.water_notification_text)
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(context.applicationContext, channel27ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(channel27ID)
            .setContentText(channel27Description)
            .setAutoCancel(true) // clear notification after click
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pi)

        // API 27+ notificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            //Makes a sound and appears as a heads-up notification   IMPORTANCE_HIGH
            //Makes a sound                                          IMPORTANCE_DEFAULT
            //No sound                                               IMPORTANCE_LOW
            //No sound and does not appear in the status bar         IMPORTANCE_MIN
            var channel = notificationManager.getNotificationChannel(channel27ID)
            if (channel == null) {
                channel = NotificationChannel(
                    channel27ID,
                    channel27Name,
                    NotificationManager.IMPORTANCE_HIGH
                )
                channel.enableVibration(true)
                channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                channel.description = channel27Description
                notificationManager.createNotificationChannel(channel)
            }
        } else { // application settings
            // vibration always on
            // TODO add vibration On/Off to Settings.
            val vibrateWater = true

            // sound
            val notifySound = preferencesRepository.getSound()

            // set sound
            if (notifySound != "") {
                var alarmSound: Uri?
                if (notifySound == "notification") { // default
                    alarmSound =
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                } else {
                    alarmSound =
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    try {
                        alarmSound = Uri.parse(notifySound)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                context.grantUriPermission(
                    "com.android.systemui", alarmSound,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                builder.setSound(alarmSound)
            }

            // vibration
            if (vibrateWater) {
                builder.setDefaults(Notification.DEFAULT_VIBRATE)
            } else {
                builder.setVibrate(longArrayOf(0L)) // Passing null here silently fails
            }
        }
        val notificationManager = NotificationManagerCompat.from(context.applicationContext)
        if (ActivityCompat.checkSelfPermission(
                context.applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(notifyID, builder.build())

        // water notification next time
        val t: Thread = object : Thread() {
            override fun run() {
                setAlarm(context)
            }
        }
        t.start()
    }


    companion object {

        /**
         * Set time notification
         * @param context
         */
        @SuppressLint("UnspecifiedImmutableFlag", "ScheduleExactAlarm")
        fun setAlarm(context: Context) {
            val preferencesRepository = PreferencesRepository(context.applicationContext as Application)

            if (preferencesRepository.firstLaunch()) {// first launch done. User save information about himself
                // get On/Off water notification
                val notificationOnOff = preferencesRepository.notify()

                if (notificationOnOff) {

                    val waterInterval: Long = DrinkPeriod.drinkTime(context.applicationContext) // next time drink interval

                    val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = waterInterval
                    val timeGoBed: String = formatter.format(calendar.time)

                    // AlarmManager
                    val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                    // intent
                    val intentWater = Intent(context, AlarmReceiver::class.java)

                    // id
                    intentWater.putExtra("id", 1)

                    // pending intent
                    val piWater: PendingIntent =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            PendingIntent.getBroadcast(
                                context,
                                1,
                                intentWater,
                                PendingIntent.FLAG_MUTABLE
                            )
                        } else {
                            PendingIntent.getBroadcast(
                                context,
                                1,
                                intentWater,
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )
                        }
                    am.cancel(piWater)

                    // set time for notification
                    am.setExact(AlarmManager.RTC_WAKEUP, waterInterval, piWater)
                } else {
                    cancelWaterNotification(context)// turn off notification
                }
            }
        }

        /**
         * Turn off notification
         * @param context
         */
        @SuppressLint("UnspecifiedImmutableFlag")
        private fun cancelWaterNotification(context: Context) {
            val intent = Intent(context, AlarmReceiver::class.java)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val piWater: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_IMMUTABLE)
            } else {
                PendingIntent.getBroadcast(context, 1, intent, 0)
            }
            alarmManager.cancel(piWater)
        }

        /**
         * Registry notification channel
         */
        @RequiresApi(api = Build.VERSION_CODES.O)
        fun registryNotificationChannel(context: Context) {

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel27ID: String = context.getString(R.string.water)
            val channel27Name = "Water notification"
            val channel27Description: String = context.getString(R.string.water_notification_text)
            val channel = NotificationChannel(
                channel27ID,
                channel27Name,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = channel27Description

            // vibration
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
