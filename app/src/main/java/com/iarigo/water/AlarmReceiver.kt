package com.iarigo.water

import android.preference.PreferenceManager

import android.os.Build

import androidx.annotation.RequiresApi

import android.annotation.SuppressLint

import androidx.core.app.NotificationManagerCompat

import android.media.RingtoneManager

import androidx.core.app.NotificationCompat

import com.iarigo.water.ui.main.MainActivity
import com.iarigo.water.helper.Helper

import android.annotation.TargetApi
import android.app.*
import android.content.*
import android.net.Uri
import java.lang.Exception
import java.util.*


/**
 * установка времени срабатывания напоминаний
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class AlarmReceiver : BroadcastReceiver() {

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onReceive(context: Context, intent: Intent) {
        // Notification

        // Activity, которое открываем по клику на уведомлении
        val openIntent = Intent(context.applicationContext, MainActivity::class.java)
        val pi: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(context, 0, openIntent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(context, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notifyID: Int = 2 // обновление предыдущих уведомлений
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
            .setContentIntent(pi) // уведомление

        // значения воды
        val mSettings =
            PreferenceManager.getDefaultSharedPreferences(context.applicationContext) // используем стандартный файл настроек


        // API 27+ notificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            //NotificationManager notificationManager = (NotificationManager) context.getSystemService(NotificationManager.class);
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
        } else { // по настройкам приложения
            // вибрация
            val vibrate_water = mSettings.getBoolean(
                "notifications_new_message_vibrate_water",
                true
            )
            // звук
            val sound_water = mSettings.getString(
                "notifications_new_message_ringtone_water",
                "notification_sound"
            )

            // звук
            if (sound_water != "") { // без звука
                var alarmSound: Uri?
                if (sound_water == "notification") { // default
                    alarmSound =
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                } else {
                    alarmSound =
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    try {
                        alarmSound = Uri.parse(sound_water)
                    } catch (e: Exception) {
                    }
                }
                context.grantUriPermission(
                    "com.android.systemui", alarmSound,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                builder.setSound(alarmSound)
            }

            // вибрация
            if (vibrate_water) {
                builder.setDefaults(Notification.DEFAULT_VIBRATE)
            } else {
                builder.setVibrate(longArrayOf(0L)) // Passing null here silently fails
            }
        }
        val notificationManager = NotificationManagerCompat.from(context.applicationContext)
        notificationManager.notify(notifyID, builder.build())

        // включаем счетчик на следующий раз
        val t: Thread = object : Thread() {
            override fun run() {
                setAlarm(context)
            }
        }
        t.start()
    }


    companion object {

        /**
         * Устанавливаем время срабатывания уведомлений
         * @param context
         */
        @SuppressLint("UnspecifiedImmutableFlag")
        fun setAlarm(context: Context) {
            val mSettings =
                PreferenceManager.getDefaultSharedPreferences(context) // используем стандартный файл настроек

            // проверяем включены или выключены уведомления
            val notification_water =
                mSettings.getBoolean("notifications_new_message_water", true) // уведомления

            if (notification_water) {
                // TODO время воды
                // val alarmHelper = AlarmHelper(context)
                val waterInterval: Long = 600

/*
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(waterInterval);
            Date todayDate = calendar.getTime();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
            String todayString = formatter.format(todayDate);

            Log.d("myTag", "Будильник. Вода. - " + todayString);
*/

                // AlarmManager
                val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                // intent
                val intentWater = Intent(context, AlarmReceiver::class.java)

                // id
                intentWater.putExtra("id", 1)

                // pending intent
                //ALARM_TYPE_RTC - время устройства
                //ALARM_TYPE_ELAPSED -  от момента загрузки устройства
                val piWater: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.getBroadcast(context, 1, intentWater, PendingIntent.FLAG_MUTABLE)
                } else {
                    PendingIntent.getBroadcast(
                        context,
                        1,
                        intentWater,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }
                am.cancel(piWater)

                // время пробуждения
                am.setExact(AlarmManager.RTC_WAKEUP, waterInterval, piWater)
            } else {
                cancelWaterNotification(context)// выключаем уведомления
            }
        }

        /**
         * Отключение уведомлений о воде
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
         * Регистрируем notification channel
         */
        @RequiresApi(api = Build.VERSION_CODES.O)
        fun registryNotificationChannel(context: Context) {

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // вода
            val channel27ID: String = context.getString(R.string.water) //
            val channel27Name: String = "Water notification" //
            val channel27Description: String = context.getString(R.string.water_notification_text)
            val channel = NotificationChannel(
                channel27ID,
                channel27Name,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = channel27Description

            // вибрация
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
