package com.wency.petmanager.work

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.wency.petmanager.ManagerApplication
import com.wency.petmanager.data.EventNotification
import com.wency.petmanager.notification.NotificationReceiver
import com.wency.petmanager.profile.TimeFormat
import java.util.concurrent.TimeUnit

class SystemAlarmSetting {

    fun assignWorkForDailyMission(){
        val alarmManager = ManagerApplication.instance.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(ManagerApplication.instance, NotificationReceiver::class.java)
        intent.putExtra(NotificationReceiver.PURPOSE, NotificationReceiver.PURPOSE_MISSION_NOTIFICATION)
        val pendingIntent = PendingIntent.getBroadcast(
            ManagerApplication.instance, NotificationReceiver.MISSION_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val time = TimeFormat.alarmTime
        time?.let {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, it.time, AlarmManager.INTERVAL_DAY, pendingIntent)
        }
    }
    fun assignWorkForEventCheck(){
        val constrains = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val checkNotificationWorkRequest: WorkRequest = PeriodicWorkRequestBuilder<EventNotificationWork>(
            CHECK_HOUR, TimeUnit.HOURS)
            .setConstraints(constrains)
            .build()

        WorkManager.getInstance(ManagerApplication.instance).enqueue(checkNotificationWorkRequest)
        resetAlarm()
    }

    private fun resetAlarm(){
        val constrains = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val resetNotificationWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<EventNotificationResetWork>()
            .setConstraints(constrains)
            .build()

        WorkManager.getInstance(ManagerApplication.instance).enqueue(resetNotificationWorkRequest)
    }

    fun createAlarmIntent(eventNotification: EventNotification): Intent{
        val intent = Intent(ManagerApplication.instance, NotificationReceiver::class.java)
        intent.apply {
            putExtra(NotificationReceiver.PURPOSE,
                NotificationReceiver.PURPOSE_EVENT_NOTIFICATION
            )
            putExtra(EventNotificationWork.EVENT_LOCATION_NAME, eventNotification.locationName)
            putExtra(EventNotificationWork.EVENT_LOCATION, eventNotification.locationLatLng)
            putExtra(EventNotificationWork.EVENT_TITLE, eventNotification.eventTitle)
            putExtra(EventNotificationWork.EVENT_ID, eventNotification.eventId)
            eventNotification.alarmTime?.let { alarmTimestamp->
                putExtra(EventNotificationWork.EVENT_TIME, TimeFormat.dateNTimeFormat.format(
                    alarmTimestamp.toDate()
                ))
            }

        }
        return intent

    }

    companion object{
        private const val CHECK_HOUR = 1L
    }
}